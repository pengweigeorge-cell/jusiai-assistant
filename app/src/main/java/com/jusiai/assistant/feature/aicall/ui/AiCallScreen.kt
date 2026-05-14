package com.jusiai.assistant.feature.aicall.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlipCameraIos
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jusiai.assistant.AssistantApp
import com.jusiai.assistant.feature.aicall.model.AiCallMode
import com.jusiai.assistant.feature.aicall.model.AiCallStatus
import com.jusiai.assistant.feature.aicall.model.ConnectingStep
import com.jusiai.assistant.feature.aicall.ui.components.AnimatedSphere
import com.jusiai.assistant.feature.aicall.ui.components.BottomControls
import com.jusiai.assistant.feature.aicall.ui.components.VideoPreview
import com.jusiai.assistant.feature.aicall.vm.AiCallViewModel
import kotlinx.coroutines.launch

private enum class PendingPermAction { Start, ToggleVideo }

@Composable
fun AiCallScreen(
    onSignOut: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val app = LocalContext.current.applicationContext as AssistantApp
    val vm: AiCallViewModel = viewModel(factory = AiCallViewModel.Factory(app))
    val state by vm.state.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    var pendingAction by remember { mutableStateOf<PendingPermAction?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
    ) { _ ->
        val micGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) ==
            PackageManager.PERMISSION_GRANTED
        val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        when (pendingAction) {
            PendingPermAction.Start -> {
                pendingAction = null
                val needCamera = state.mode == AiCallMode.Video
                if (micGranted && (!needCamera || cameraGranted)) {
                    vm.startCall()
                } else {
                    scope.launch { snackbarHostState.showSnackbar("需要麦克风/摄像头权限") }
                }
            }
            PendingPermAction.ToggleVideo -> {
                pendingAction = null
                // Only hits this branch when going Voice → Video; voice→voice needs no permission.
                if (cameraGranted) vm.toggleMode()
                else scope.launch { snackbarHostState.showSnackbar("需要摄像头权限") }
            }
            null -> Unit
        }
    }

    fun launchWithPerms() {
        val needed = buildList {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) !=
                PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.RECORD_AUDIO)
            if (state.mode == AiCallMode.Video &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) !=
                PackageManager.PERMISSION_GRANTED
            ) add(Manifest.permission.CAMERA)
        }
        if (needed.isEmpty()) {
            vm.startCall()
        } else {
            pendingAction = PendingPermAction.Start
            permissionLauncher.launch(needed.toTypedArray())
        }
    }

    fun handleToggleVideo() {
        val goingToVideo = state.mode == AiCallMode.Voice
        val cameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED
        if (goingToVideo && !cameraGranted) {
            pendingAction = PendingPermAction.ToggleVideo
            permissionLauncher.launch(arrayOf(Manifest.permission.CAMERA))
        } else {
            vm.toggleMode()
        }
    }

    // End call on backgrounding (v1 behaviour).
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP) {
                val st = vm.state.value.status
                if (st is AiCallStatus.Active || st is AiCallStatus.Connecting) {
                    vm.endCall()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
    }

    // Surface transient toast as a snackbar.
    LaunchedEffect(state.errorToast) {
        val msg = state.errorToast ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.dismissError()
    }

    // Consume Ended → collapse to Idle once the UI has rendered Ended for a beat.
    LaunchedEffect(state.status) {
        if (state.status is AiCallStatus.Ended || state.status is AiCallStatus.Failed) {
            kotlinx.coroutines.delay(400)
            vm.consumeEnded()
        }
    }

    val isVideoActive = state.status is AiCallStatus.Active && state.mode == AiCallMode.Video

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize()) {
            // Background — video fill in video-active mode, otherwise white.
            if (isVideoActive) {
                VideoPreview(
                    room = vm.liveKitRoom,
                    track = vm.localVideoTrack,
                    mirror = state.cameraFront,
                    modifier = Modifier.fillMaxSize(),
                )
            }

            Column(modifier = Modifier.fillMaxSize().padding(inner)) {
                TopBar(
                    onOpenSettings = { vm.showPicker(true) },
                    canOpenSettings = state.status is AiCallStatus.Idle ||
                        state.status is AiCallStatus.Failed ||
                        state.status is AiCallStatus.Ended,
                    tintOnDark = isVideoActive,
                    showFlipCamera = isVideoActive,
                    onFlipCamera = vm::flipCamera,
                )

                Box(
                    modifier = Modifier.fillMaxWidth().weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    when {
                        isVideoActive -> Unit // video fills the background
                        else -> CenterContent(
                            status = state.status,
                            audioLevel = { state.agentAudioLevel },
                            onTap = vm::onTapToInterrupt,
                        )
                    }
                }

                StatusHint(
                    status = state.status,
                    mode = state.mode,
                    onDark = isVideoActive,
                )

                BottomControls(
                    status = state.status,
                    mode = state.mode,
                    isMicMuted = state.isMicMuted,
                    onToggleMic = vm::toggleMic,
                    onPrimaryAction = {
                        when (state.status) {
                            is AiCallStatus.Idle,
                            is AiCallStatus.Failed,
                            is AiCallStatus.Ended,
                            -> launchWithPerms()
                            else -> vm.endCall()
                        }
                    },
                    onToggleVideoMode = { handleToggleVideo() },
                )

                Text(
                    text = "内容由 AI 生成",
                    fontSize = 12.sp,
                    color = if (isVideoActive) Color.White.copy(alpha = 0.7f) else Color(0xFF999999),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
            }

            if (state.showPicker) {
                VoicePickerSheet(
                    config = state.agentConfig,
                    selectedVoiceIndex = state.selectedVoiceIndex,
                    selectedPromptLabel = state.selectedPromptLabel,
                    onSelectVoice = vm::selectVoice,
                    onSelectPrompt = vm::selectPrompt,
                    onSignOut = {
                        vm.showPicker(false)
                        onSignOut()
                    },
                    onDismiss = { vm.showPicker(false) },
                )
            }
        }
    }
}

@Composable
private fun TopBar(
    onOpenSettings: () -> Unit,
    canOpenSettings: Boolean,
    tintOnDark: Boolean,
    showFlipCamera: Boolean,
    onFlipCamera: () -> Unit,
) {
    val tint = if (tintOnDark) Color.White else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (showFlipCamera) {
            IconButton(onClick = onFlipCamera) {
                Icon(
                    imageVector = Icons.Filled.FlipCameraIos,
                    contentDescription = "切换摄像头",
                    tint = tint,
                )
            }
        } else {
            // Mirror the trailing IconButton's footprint so the title stays centred.
            Spacer(modifier = Modifier.size(48.dp))
        }
        Spacer(modifier = Modifier.weight(1f))
        Text(
            text = "AI 助手",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = tint,
        )
        Spacer(modifier = Modifier.weight(1f))
        IconButton(
            onClick = onOpenSettings,
            enabled = canOpenSettings,
        ) {
            Icon(
                imageVector = Icons.Filled.Settings,
                contentDescription = "设置",
                tint = if (canOpenSettings) tint else tint.copy(alpha = 0.4f),
            )
        }
    }
}

@Composable
private fun CenterContent(
    status: AiCallStatus,
    audioLevel: () -> Float,
    onTap: () -> Unit,
) {
    AnimatedSphere(audioLevel = audioLevel, onTap = onTap)
}

@Composable
private fun StatusHint(
    status: AiCallStatus,
    mode: AiCallMode,
    onDark: Boolean,
) {
    val (label, isConnecting) = when (status) {
        is AiCallStatus.Idle -> (if (mode == AiCallMode.Voice) "点击开始语音通话" else "点击开始视频通话") to false
        is AiCallStatus.Connecting -> connectingLabel(status.step) to true
        is AiCallStatus.Active -> (if (status.mode == AiCallMode.Voice) "说话或点击打断" else "正在聆听") to false
        is AiCallStatus.Ended -> "通话已结束" to false
        is AiCallStatus.Failed -> status.message to false
    }
    val background = if (onDark) Color.Black.copy(alpha = 0.4f) else Color(0xFFF1F1F1)
    val textColor = if (onDark) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(background)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(14.dp),
                    strokeWidth = 2.dp,
                    color = textColor,
                )
                Spacer(modifier = Modifier.size(8.dp))
            }
            Text(text = label, color = textColor, fontSize = 14.sp)
        }
    }
}

private fun connectingLabel(step: ConnectingStep): String = when (step) {
    ConnectingStep.CreatingRoom -> "正在创建房间"
    ConnectingStep.JoiningLiveKit -> "正在接入"
    ConnectingStep.PublishingTracks -> "正在准备音视频"
    ConnectingStep.StartingAgent -> "正在启动 AI 助手"
    ConnectingStep.WaitingAgent -> "正在等待 AI 加入"
    ConnectingStep.SwitchingMode -> "正在切换模式"
}
