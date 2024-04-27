package cc.unitmesh.devti.agent

import cc.unitmesh.devti.agent.configurable.customAgentSetting
import cc.unitmesh.devti.agent.model.CustomAgentConfig
import cc.unitmesh.devti.intentions.action.test.TestCodeGenContext
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json

//this is hard code should be refactoring
class CustomAgentUtil {
    companion object {
        fun loadRagApp(project: Project, logger: Logger): CustomAgentConfig? {
            val ragsJsonConfig = project.customAgentSetting.ragsJsonConfig
            if (ragsJsonConfig.isEmpty()) return null

            val rags = try {
                Json.decodeFromString<List<CustomAgentConfig>>(ragsJsonConfig)
            } catch (e: Exception) {
                logger.warn("Failed to parse custom rag apps", e)
                listOf()
            }

            if (rags.isEmpty()) {
                return null;
            }
            return rags[0]
        }

        fun getRAGContext(testPromptContext: TestCodeGenContext, project: Project, logger: Logger): String {
            val agent = loadRagApp(project, logger)
            if (agent != null) {
                val query = testPromptContext.sourceCode
                val stringFlow: Flow<String>? = CustomAgentExecutor(project).execute(query, agent)
                if (stringFlow != null) {
                    val responseBuilder = StringBuilder()
                    runBlocking {
                        stringFlow.collect { string ->
                            responseBuilder.append(string)
                        }
                    }
                    return responseBuilder.toString()
                }
            }
            return ""
        }
    }
}
