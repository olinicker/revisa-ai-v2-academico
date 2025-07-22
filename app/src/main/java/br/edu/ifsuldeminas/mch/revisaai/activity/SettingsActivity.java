package br.edu.ifsuldeminas.mch.revisaai.activity;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.concurrent.TimeUnit;

import br.edu.ifsuldeminas.mch.revisaai.R;
import br.edu.ifsuldeminas.mch.revisaai.util.NotificationWorker;

public class SettingsActivity extends AppCompatActivity {

    private SwitchMaterial switchReminder;
    private SharedPreferences sharedPreferences;

    public static final String PREFS_NAME = "RevisaAiPrefs";
    public static final String REMINDER_KEY = "reminder_enabled";
    private static final String REMINDER_WORK_TAG = "revisa_ai_reminder";
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // Permissão concedida, agendar o lembrete
                    scheduleReminder();
                } else {
                    // Permissão negada, desativar o switch
                    Toast.makeText(this, "Permissão de notificação negada.", Toast.LENGTH_SHORT).show();
                    switchReminder.setChecked(false);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        switchReminder = findViewById(R.id.switchReminder);

        boolean reminderEnabled = sharedPreferences.getBoolean(REMINDER_KEY, false);
        switchReminder.setChecked(reminderEnabled);

        switchReminder.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Salva o estado no SharedPreferences imediatamente
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(REMINDER_KEY, isChecked);
            editor.apply();

            if (isChecked) {
                askForNotificationPermission();
            } else {
                cancelReminder();
            }
        });
    }

    private void askForNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Verifica se a permissão já foi concedida
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // Permissão já concedida, pode agendar
                scheduleReminder();
            } else {
                // Pede a permissão
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        } else {
            // Em versões mais antigas, a permissão é concedida na instalação
            scheduleReminder();
        }
    }

    private void scheduleReminder() {
        // Cria uma tarefa periódica para ser executada a cada 24 horas
        PeriodicWorkRequest reminderRequest =
                new PeriodicWorkRequest.Builder(NotificationWorker.class, 24, TimeUnit.HOURS)
                        .setInitialDelay(24, TimeUnit.HOURS) // Opcional: Adia a primeira execução
                        .build();

        // Agenda a tarefa com um nome único para evitar duplicação
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                REMINDER_WORK_TAG,
                androidx.work.ExistingPeriodicWorkPolicy.REPLACE,
                reminderRequest);

        Toast.makeText(this, "Lembretes diários ativados!", Toast.LENGTH_SHORT).show();
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
