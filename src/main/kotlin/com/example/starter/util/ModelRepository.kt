import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.pytorch.Module

object ModelRepository {
  private val modelMap = mutableMapOf<String, Module>()

  suspend fun readModelFromResources(modelName: String): Pair<String, Module?> {
    if (modelMap.containsKey(modelName)) {
      return Pair(modelName, modelMap[modelName])
    }

    val model: Module? = loadModuleFromResources(modelName)
    if (model != null) {
      modelMap[modelName] = model
      return Pair(modelName, model)
    } else {
      throw Exception("Model not found")
    }
  }

  private suspend fun loadModuleFromResources(modelName: String): Module? = withContext(Dispatchers.IO) {
    System.out.println("User Dir============= " + System.getProperty("user.dir"))
    try {
      Module.load(System.getProperty("user.dir") + "/" + modelName)
    } catch (e: Exception) {
      null
    }
  }
}

fun Module.size(modelName: String): Int {
  var subName: String = modelName.substring(modelName.length - 7, modelName.length - 4)
  if (subName.startsWith("_")) {
    subName = subName.substring(1)
  }
  return subName.toInt()
}
