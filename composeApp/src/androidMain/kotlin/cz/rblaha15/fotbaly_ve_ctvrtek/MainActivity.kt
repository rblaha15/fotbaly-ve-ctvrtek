package cz.rblaha15.fotbaly_ve_ctvrtek

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import cz.rblaha15.fotbaly_ve_ctvrtek.ui.theme.FotbalyTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val viewModel = viewModel {
                ManViewModel(
                    repository = createRepository(),
                )
            }
            val answers by viewModel.answers.collectAsStateWithLifecycle()
            val name by viewModel.name.collectAsStateWithLifecycle()
            val myAnswer by viewModel.myAnswer.collectAsStateWithLifecycle()
            val areNotificationsEnabled by viewModel.areNotificationsEnabled.collectAsStateWithLifecycle()
            val count by viewModel.count.collectAsStateWithLifecycle()

            FotbalyTheme {
                Surface(Modifier.fillMaxSize()) {
                    AppContent(
                        answers = answers,
                        name = name,
                        onNameChange = viewModel::setName,
                        myAnswer = myAnswer,
                        count = count,
                        onMyAnswerChange = viewModel::setMyAnswer,
                        areNotificationsEnabled = areNotificationsEnabled,
                        setNotificationsEnabled = viewModel::setNotificationsEnabled,
                    )
                }
            }
        }
    }
}