package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.util.NotificationWorker;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchReminder;
    private TextView textViewReminderTime;
    private TextInputLayout textInputLayoutReminderTime; // Referência ao TextInputLayout
    private SharedPreferences sharedPreferences;

    public static final String PREFS_NAME = "RevisaAiPrefs";
    public static final String REMINDER_KEY = "reminder_enabled";
    public static final String REMINDER_HOUR_KEY = "reminder_hour";
    public static final String REMINDER_MINUTE_KEY = "reminder_minute";
    private static final String REMINDER_WORK_TAG = "revisa_ai_reminder";

    private int selectedHour;
    private int selectedMinute;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permissão concedida, agendar o lembrete com o horário selecionado
                    scheduleReminder();
                } else {
                    // Permissão negada, desativar o switch e o campo de horário
                    Toast.makeText(this, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show();
                    switchReminder.setChecked(false);
                    textInputLayoutReminderTime.setEnabled(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        switchReminder = findViewById(R.id.switchReminder);
        textViewReminderTime = findViewById(R.id.textViewReminderTime);
        textInputLayoutReminderTime = findViewById(R.id.textInputLayoutReminderTime);

        // Carrega o estado do switch e o horário salvo
        boolean reminderEnabled = sharedPreferences.getBoolean(REMINDER_KEY, false);
        switchReminder.setChecked(reminderEnabled);

        selectedHour = sharedPreferences.getInt(REMINDER_HOUR_KEY, 9); // Padrão: 9h
        selectedMinute = sharedPreferences.getInt(REMINDER_MINUTE_KEY, 0); // Padrão: 00min
        updateReminderTimeDisplay(); // Atualiza o TextView com o horário salvo

        // Habilita/desabilita o campo de horário com base no switch
        textInputLayoutReminderTime.setEnabled(reminderEnabled);

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(REMINDER_KEY, isChecked);
            editor.apply();

            textInputLayoutReminderTime.setEnabled(isChecked); // Habilita/desabilita o campo de horário

            if (isChecked) {
                askForNotificationPermission();
            } else {
                cancelReminder();
            }
        });

        textViewReminderTime.setOnClickListener(v -> showTimePickerDialog());
    }

    private void updateReminderTimeDisplay() {
        // Formata o horário para exibição (ex: 09:00, 14:30)
        String formattedTime = String.format("%02d:%02d", selectedHour, selectedMinute);
        textViewReminderTime.setText(formattedTime);
    }

    private void showTimePickerDialog() {
        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, hourOfDay, minute) -> {
                    selectedHour = hourOfDay;
                    selectedMinute = minute;
                    updateReminderTimeDisplay(); // Atualiza o TextView
                    saveReminderTime(); // Salva o novo horário
                    // Se o lembrete estiver ativo, reagenda com o novo horário
                    if (switchReminder.isChecked()) {
                        scheduleReminder();
                    }
                }, selectedHour, selectedMinute, true); // true para formato 24h
        timePickerDialog.show();
    }

    private void saveReminderTime() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(REMINDER_HOUR_KEY, selectedHour);
        editor.putInt(REMINDER_MINUTE_KEY, selectedMinute);
        editor.apply();
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                scheduleReminder();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            scheduleReminder();
        }
    }

    private void scheduleReminder() {
        // Calcula o atraso inicial até o próximo horário de notificação
        Calendar now = Calendar.getInstance();
        Calendar notificationTime = (Calendar) now.clone();
        notificationTime.set(Calendar.HOUR_OF_DAY, selectedHour);
        notificationTime.set(Calendar.MINUTE, selectedMinute);
        notificationTime.set(Calendar.SECOND, 0);
        notificationTime.set(Calendar.MILLISECOND, 0);

        // Se o horário de notificação já passou hoje, agende para amanhã
        if (notificationTime.before(now)) {
            notificationTime.add(Calendar.DAY_OF_MONTH, 1);
        }

        long initialDelay = notificationTime.getTimeInMillis() - now.getTimeInMillis();

        // Cria uma tarefa periódica para ser executada a cada 24 horas
        PeriodicWorkRequest reminderRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(initialDelay, TimeUnit.MILLISECONDS)
                        .build();

        // Agenda a tarefa com um nome único para evitar duplicação
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                REMINDER_WORK_TAG,
                ExistingPeriodicWorkPolicy.REPLACE, // Usa REPLACE para atualizar o agendamento
                reminderRequest);

        Toast.makeText(this, "Lembretes agendados para " + String.format("%02d:%02d", selectedHour, selectedMinute) + "!", Toast.LENGTH_SHORT).show();
    }

    private void cancelReminder() {
        // Cancela a tarefa agendada
        WorkManager.getInstance(this).cancelUniqueWork(REMINDER_WORK_TAG);
        Toast.makeText(this, "Lembretes desativados.", Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
