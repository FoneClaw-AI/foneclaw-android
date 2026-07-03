package ai.android.claw.plugin.template.simple

import ai.android.claw.extension.IFoneClawExtensionService
import ai.android.claw.plugin.PluginBundleKeys
import android.app.Service
import android.content.Intent
import android.os.Bundle
import android.os.IBinder
import org.json.JSONObject

class SimplePluginService : Service() {

    private val binder = object : IFoneClawExtensionService.Stub() {
        override fun executeTool(request: Bundle?): Bundle {
            val toolName = request?.getString(PluginBundleKeys.TOOL_NAME).orEmpty()
            val argsJson = request?.getString(PluginBundleKeys.ARGS_JSON).orEmpty()
                .ifBlank { "{}" }

            return when (toolName) {
                "plugin_template_echo" -> echo(argsJson)
                else -> errorResponse(
                    status = "INVALID_ARGUMENT",
                    code = "unknown_tool",
                    message = "Unknown template plugin tool: $toolName",
                )
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    private fun echo(argsJson: String): Bundle {
        val message = runCatching {
            JSONObject(argsJson).optString("message", "")
        }.getOrDefault("")

        return Bundle().apply {
            putString(PluginBundleKeys.STATUS, "OK")
            putString(PluginBundleKeys.TEXT, "Template echo: $message")
        }
    }

    private fun errorResponse(
        status: String,
        code: String,
        message: String,
    ): Bundle {
        return Bundle().apply {
            putString(PluginBundleKeys.STATUS, status)
            putString(PluginBundleKeys.TEXT, "")
            putString(PluginBundleKeys.ERROR_CODE, code)
            putString(PluginBundleKeys.ERROR_MESSAGE, message)
        }
    }
}
