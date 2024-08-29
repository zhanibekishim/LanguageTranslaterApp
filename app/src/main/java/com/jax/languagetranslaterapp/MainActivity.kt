package com.jax.languagetranslaterapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.jax.languagetranslaterapp.databinding.ActivityMainBinding
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private val binding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

    private val speechRecognizerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK && result.data != null) {
                val spokenText: String? = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.get(0)
                spokenText?.let {
                    binding.sourceCodeEditText.setText(it)
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.fromSpinner.adapter = ArrayAdapter(this, R.layout.spinner_item, fromLanguages)
        binding.toSpinner.adapter = ArrayAdapter(this, R.layout.spinner_item, toLanguages)

        binding.fromSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                fromLanguagesCode = getLanguageCode(fromLanguages[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.toSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                toLanguagesCode = getLanguageCode(toLanguages[position])
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        binding.translateButton.setOnClickListener {
            if (fromLanguagesCode == 0 || toLanguagesCode == 0 || binding.sourceCodeEditText.text.toString().isEmpty()) {
                Toast.makeText(this, "Please choose language", Toast.LENGTH_SHORT).show()
            } else {
                translateText(fromLanguagesCode, toLanguagesCode, binding.sourceCodeEditText.text.toString())
            }
        }

        binding.micImageView.setOnClickListener {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                putExtra(RecognizerIntent.EXTRA_PROMPT, "Say something to translate it")
            }
            try {
                speechRecognizerLauncher.launch(intent)
            } catch (e: Exception) {
                Toast.makeText(this, "Something went wrong", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLanguageCode(language: String): Int {
        return when (language) {
            "english" -> FirebaseTranslateLanguage.EN
            "hindi" -> FirebaseTranslateLanguage.HI
            "spanish" -> FirebaseTranslateLanguage.ES
            else -> FirebaseTranslateLanguage.KA
        }
    }

    private fun translateText(fromLanguage: Int, toLanguage: Int, text: String) {
        val options = FirebaseTranslatorOptions.Builder()
            .setSourceLanguage(fromLanguage)
            .setTargetLanguage(toLanguage)
            .build()
        val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)
        val conditions = FirebaseModelDownloadConditions.Builder().requireWifi().build()

        translator.downloadModelIfNeeded().addOnSuccessListener {
            translator.translate(text).addOnSuccessListener { translatedText ->
                binding.translatedTextView.text = translatedText
            }.addOnFailureListener {
                Toast.makeText(this, "Failed to translate text", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener {
            Toast.makeText(this, "Failed to download translation model", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        val fromLanguages = listOf("from", "english", "hindi", "spanish")
        val toLanguages = listOf("to", "english", "hindi", "spanish")
        var fromLanguagesCode = 0
        var toLanguagesCode = 0
    }
}
