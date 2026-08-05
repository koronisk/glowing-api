# GlowingApi

API для простого и удобного управления эффектом свечения.

> [!NOTE]  
> Ожидаемая версия сервера: 26.2+
 
> [!IMPORTANT]  
> Для работы необходим вспомогательный плагин [kotlin-stdlib-bukkit](https://github.com/koronisk/kotlin-stdlib-bukkit)

## Возможности
- Беспрерывное свечение сущностей и игроков
- Выбор любого цвета свечения
- Ресинхронизация при перезаходе

## Пример использования

### GlowingTask (Одна задача)



```kotlin
val glowingApi = Bukkit.getPluginManager().getPlugin("GlowingApi") as GlowingApiPlugin

val player: Player = ...

val task: GlowingTask = glowingApi.initTask { 
    // Красный цвет
    color = GlowingColor.RED
    // Один наблюдатель
    observers.add(player)
            
    // Подсветка множества игроков  
    playerTargets.addAll(Bukkit.getOnlinePlayers())
    // Подсветка множества сущностей
    entityTargets.addAll(setOf())
}

task.start()
```

### GlowingMultiTask (Мультизадача)

```kotlin
val glowingApi = Bukkit.getPluginManager().getPlugin("GlowingApi") as GlowingApiPlugin

val player: Player = ...
val player2: Player = ...

val entities = e.player.world.entities.filterIsInstance<LivingEntity>()

val multiTask = GlowingMultiTask(glowingApi).setup {
    // Задача 1
    addTask {
        color = GlowingColor.LIGHT_PURPLE
        observers.add(player)

        playerTargets.add(player2)
    }

    // Задача 2
    addTask {
        color = GlowingColor.GOLD
        observers.add(player)

        entityTargets.addAll(entities)
    }
}

// Задачи будут работать одновременно
multiTask.start()
```

> [!WARNING]  
> Не забывайте использовать `task.end()` для окончания свечения

### Модифицировать уже работающую задачу

```kotlin
task.addObserver(player: Player)
task.removeObserver(playerName: String)
task.removeTarget(playerName: String)
task.removeTarget(entityId: Int)

// После этих действий ресинхронизируйте задачу
task.resync()
```