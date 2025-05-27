![](logo.png)
# HeavenRPG
_This plugin allows server owners and plugin developers to make their own races and classes with spells underneath this framework._

## Commands
### Player Commands
- `/keybind <keybind> <spell>`

### Admin Commands
- `/heavenrpg reload` Reloads the config files.
- `/heavenrpg item <item>` Spawns in any custom item by this plugin.
- `/heavenrpg race <race> [player]` Change you (or another player) into a specific race.
- `/heavenrpg class <class> [class]` Change you (or another player) into a specific class.

## Configuration
When configuring, any files in an inner folder like `races`, `classes`, and `spells` use an a tag on the first line to let it know what kind of class it's loading. This to automate the process of making these things.

The main changes to `spells` files is that you have to know what base class name you're pulling from. So if it's a PotionSpell or a PassiveSpell, you have to specify that in the first line for it to load correctly.

### Items
Items can be configured in `items.yml`. They can also be used as Cast Items for classes within a class config file.
```YAML
ExampleItem:
  material: STICK
  amount: 1
  display-name: "&fExample Item"
  lore:
    - "&7This is an example item."
  custom-model-data: 0
```

### Classes
Here is how you can make a class. This will load every time you reload the plugin with `/heavenrpg reload` or when the server boots up.

Remember that the first line is a tag that tells the plugin what base class to use. 

**MAKE SURE THE FIRST LINE IS THIS!**
```YAML
!!com.github.rfsmassacre.heavenrpg.classes.OriginClass
attributeStats: []
castItemName: ExampleItem
displayName: '&fExample Class'
name: ExampleClass
spellNames:
  - ExampleClassSpell
```

### Races
Here is how you can make a race. This works similar to classes in that they load similarly.

**MAKE SURE THE FIRST LINE IS THIS!**
```YAML
!!com.github.rfsmassacre.heavenrpg.races.OriginRace
attributeStats: []
displayName: '&fExample Race'
name: ExampleRace
spellNames:
  - ExampleRaceSpell
```

### Spells
Here is how you can make a spell. This requires you specify a spell class on the first line as a tag.

Here is an example spell called Prayer that gives the caster a random beneficial potion effect.

Key things to remember:
- All spells can be set to target yourself by settings `targetSelf: true`.
- All spells by default have no cooldown. Change this by how many milliseconds of cooldown time you want. 10 seconds would be `cooldown: 10000`.
- Setting the spell to have `beneficial: true` will let other plugins know using team or protection mechanics that it's a spell for allies or friendly targets if they hook into this one and check the SpellTargetEvent.
```YAML
!!com.github.rfsmassacre.heavenrpg.spells.PotionSpell
amplifyMax: 1
amplifyMin: 0
beneficial: true
bindable: true
cooldown: 10000
cooldownMessage: '&f{spell}&r &cis on cooldown! &4(&e{time}&4)'
customModelId: 0
displayName: "&ePrayer"
dot: 0.2
durationMax: 6000
durationMin: 3000
internalName: Prayer
itemMessage: '&f{spell}&r &cis missing an item!'
level: 0
levelMessage: '&f{spell}&r &crequires you to be &eLVL {level}&c!'
noPotionFound: '&f{target}&c has every potion effect!'
noTargetMessage: '&f{spell}&r &crequires a target!'
potionCategoryName: BENEFICIAL
potionReceived: '&f{sender}&e has given you &b{potion} {amplify}&e!'
potionSent: '&7You gave &f{target}&7 &b{potion} {amplify}&7!'
potionTypeName: null
range: 0.0
targetSelf: true
```