package ru.let.glowingapi

class GlowingMultiTask(private val plugin: GlowingApiPlugin) {
    private val tasks: MutableSet<GlowingTask> = mutableSetOf()
    
    fun setup(action: (GlowingMultiTask).() -> Unit): GlowingMultiTask {
        action(this)
        
        return this
    }

    fun addTask(action: (GlowingTaskBuilder).() -> Unit) {
        val task = plugin.initTask(action)
        tasks.add(task)
    }

    fun start() {
        tasks.forEach { it.start() }
    }

    fun end() {
        tasks.forEach { it.end() }
    }
}