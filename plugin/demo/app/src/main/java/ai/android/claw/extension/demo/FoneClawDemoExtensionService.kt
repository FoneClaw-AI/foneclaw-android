package ai.android.claw.extension.demo

import ai.android.claw.extension.IFoneClawExtensionService
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import org.json.JSONObject

class FoneClawDemoExtensionService : Service() {

    private val binder = object : IFoneClawExtensionService.Stub() {
        override fun executeTool(request: Bundle?): Bundle {
            val toolName = request?.getString(KEY_TOOL_NAME).orEmpty()
            val argsJson = request?.getString(KEY_ARGS_JSON).orEmpty().ifBlank { "{}" }
            return when (toolName) {
                "plugin_demo_echo" -> executeEcho(argsJson)
                else -> errorResponse(
                    status = "INVALID_ARGUMENT",
                    code = "unknown_tool",
                    message = "Unknown plugin tool: $toolName",
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun executeEcho(argsJson: String): Bundle {
        val message = runCatching {
            JSONObject(argsJson).optString("message", "")
        }.getOrDefault("")

        return Bundle().apply {
            putString(KEY_STATUS, "OK")
            putString(KEY_TEXT, "Demo plugin echo: $message")
        }
    }

    private fun errorResponse(
        status: String,
        code: String,
        message: String,
    ): Bundle {
        return Bundle().apply {
            putString(KEY_STATUS, status)
            putString(KEY_TEXT, "")
            putString(KEY_ERROR_CODE, code)
            putString(KEY_ERROR_MESSAGE, message)
        }
    }

    private companion object {
        const val KEY_TOOL_NAME = "toolName"
        const val KEY_ARGS_JSON = "argsJson"
        const val KEY_STATUS = "status"
        const val KEY_TEXT = "text"
        const val KEY_ERROR_CODE = "errorCode"
        const val KEY_ERROR_MESSAGE = "errorMessage"
    }
}
