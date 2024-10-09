package cz.rblaha15.fotbaly_ve_ctvrtek

import android.Manifest
import android.os.Build
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppContent(
    answers: List<Pair<Person, AnswerState>>,
    myAnswer: AnswerState?,
    onMyAnswerChange: (AnswerState?) -> Unit,
    name: String,
    count: Int,
    onNameChange: (String) -> Unit,
    areNotificationsEnabled: Boolean,
    setNotificationsEnabled: (Boolean) -> Unit,
) {
    Scaffold(
        Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Fotbaly ve čtvrtek") },
                actions = {
                    val context = LocalContext.current
                    IconButton(
                        onClick = {
                            CustomTabsIntent.Builder()
                                .setShowTitle(true)
                                .build()
                                .launchUrl(context, "https://fotbaly-ve-ctvrtek.web.app/".toUri())
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = "Otevřít v prohlížeči",
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(all = 16.dp),
        ) {
            NotificationsToggle(
                areNotificationsEnabled = areNotificationsEnabled,
                setNotificationsEnabled = setNotificationsEnabled,
                name = name,
            )
            NameInput(
                name = name,
                onNameChange = onNameChange,
            )
            Counter(count = count)
            MyAnswer(
                myAnswer = myAnswer,
                onMyAnswerChange = onMyAnswerChange,
            )
            AnswersList(
                answers = answers,
            )
        }
    }
}

@Composable
fun Counter(count: Int) {
    Text("Kolik nás bude: $count", Modifier.padding(vertical = 8.dp))
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NotificationsToggle(
    areNotificationsEnabled: Boolean,
    setNotificationsEnabled: (Boolean) -> Unit,
    name: String,
) {
    var waitingForPermission by remember { mutableStateOf(false) }
    var showError by remember { mutableStateOf(false) }

    val permissionState = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        rememberPermissionState(Manifest.permission.POST_NOTIFICATIONS)
    else null

    LaunchedEffect(waitingForPermission, permissionState?.status) {
        if (permissionState != null && permissionState.status.isGranted && waitingForPermission) {
            waitingForPermission = false
            setNotificationsEnabled(true)
        }
    }
    LaunchedEffect(name, showError) {
        if (showError && name.isNotBlank()) {
            showError = false
        }
    }

    val shakeOffset = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    val onCheckedChange = { it: Boolean ->
        if (it && name.isBlank()) scope.launch {
            showError = true
            repeat(4) {
                shakeOffset.animateTo(-15f, animationSpec = tween(50))
                shakeOffset.animateTo(15f, animationSpec = tween(50))
            }
            shakeOffset.animateTo(0f, animationSpec = tween(50))
        } else if (it && permissionState != null && !permissionState.status.isGranted) {
            permissionState.launchPermissionRequest()
            waitingForPermission = true
        } else {
            setNotificationsEnabled(it)
        }
        Unit
    }
    Surface(
        checked = areNotificationsEnabled,
        onCheckedChange = onCheckedChange,
    ) {
        Row(
            Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text("Zasílat každý týden notifikace?", Modifier.weight(1F))
            Switch(
                checked = areNotificationsEnabled,
                onCheckedChange = onCheckedChange,
            )
        }
    }

    AnimatedVisibility(
        visible = showError,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Text(
            text = "Musíte zadat jméno!",
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
        )
    }
}

@Composable
fun MyAnswer(
    myAnswer: AnswerState?,
    onMyAnswerChange: (AnswerState?) -> Unit,
) {
    ListItem(
        headlineContent = { Text("Půjdeš ve čtvrtek na fotbal?") },
        Modifier
            .padding(vertical = 8.dp)
            .clip(CircleShape),
        colors = ListItemDefaults.colors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            headlineColor = MaterialTheme.colorScheme.onSecondaryContainer,
        ),
        trailingContent = {
            Row {
                AnswerState.entries.forEach { state ->
                    TextButton(
                        onClick = {
                            onMyAnswerChange(if (state == myAnswer) null else state)
                        },
                    ) {
                        Icon(
                            imageVector = when (state) {
                                AnswerState.Yes -> Icons.Default.Check
                                AnswerState.No -> Icons.Default.Close
                                AnswerState.Maybe -> Icons.AutoMirrored.Default.HelpOutline
                            },
                            contentDescription = when (state) {
                                AnswerState.Yes -> "Přijdu"
                                AnswerState.No -> "Nepřijdu"
                                AnswerState.Maybe -> "Nevím"
                            },
                            tint = when {
                                myAnswer != state -> MaterialTheme.colorScheme.onSecondaryContainer
                                else -> when (state) {
                                    AnswerState.Yes -> MaterialTheme.colorScheme.primary
                                    AnswerState.No -> MaterialTheme.colorScheme.error
                                    AnswerState.Maybe -> MaterialTheme.colorScheme.tertiary
                                }
                            },
                        )
                    }
                }
            }
        }
    )
}

@Composable
fun NameInput(name: String, onNameChange: (String) -> Unit) = Row {
    var localName by remember(name) { mutableStateOf(name) }
    val isEditing by remember(name, localName) {
        derivedStateOf { localName != name }
    }
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current
    OutlinedTextField(
        value = localName,
        onValueChange = { localName = it },
        Modifier
            .fillMaxWidth(),
        label = { Text("Jméno") },
        trailingIcon = {
            if (isEditing) IconButton(
                onClick = {
                    onNameChange(localName)
                    focusManager.clearFocus(true)
                    keyboardController?.hide()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Uložit",
                )
            }
        }
    )
}

@Composable
fun AnswersList(answers: List<Pair<Person, AnswerState>>) = LazyColumn {
    items(answers) { (name, answer) ->
        ListItem(
            headlineContent = { Text(name) },
            trailingContent = {
                Row {
                    AnswerState.entries.forEach { state ->
                        Icon(
                            imageVector = when (state) {
                                AnswerState.Yes -> Icons.Default.Check
                                AnswerState.No -> Icons.Default.Close
                                AnswerState.Maybe -> Icons.AutoMirrored.Default.HelpOutline
                            },
                            contentDescription = when (state) {
                                AnswerState.Yes -> "Přijde"
                                AnswerState.No -> "Nepřijde"
                                AnswerState.Maybe -> "Neví"
                            },
                            Modifier.padding(all = 16.dp),
                            tint = when {
                                answer != state -> MaterialTheme.colorScheme.onSurface
                                else -> when (state) {
                                    AnswerState.Yes -> MaterialTheme.colorScheme.primary
                                    AnswerState.No -> MaterialTheme.colorScheme.error
                                    AnswerState.Maybe -> MaterialTheme.colorScheme.tertiary
                                }
                            },
                        )
                    }
                }
            }
        )
    }
    if (answers.isEmpty()) item {
        Text(
            text = "Zatím nikdo neodpověděl",
            modifier = Modifier.fillMaxWidth(),
        )
    }
}