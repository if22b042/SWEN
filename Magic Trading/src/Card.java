public class Card {
    public String name;
    public int id;
    public String type;
    public String element;
    public int damage;
    public String kind;
    public Card(String name, int id, String type, String element, int damage, String kind)
    {
        this.name=name;
        this.id=id;
        this.type=type;
        this.element=element;
        this.damage=damage;
        this.kind=kind;

    }

    public  void main(String[] args) {

        Card card1 = new Card("WaterGoblin", 1, "Monster", "Water", 10, "Goblin");
        Card card2 = new Card("FireTroll", 2, "Monster", "Fire", 15, "Troll");
        Card card3 = new Card("RegularGoblin", 3, "Monster", "Normal", 12, "Goblin");
        Card card4 = new Card("Knight", 4, "Monster", "Normal", 20, "Human");
        Card card5 = new Card("Wizard", 5, "Monster", "Normal", 18, "Human");
        Card card6 = new Card("Dragon", 6, "Monster", "Fire", 25, "Dragon");
        Card card7 = new Card("Kraken", 7, "Monster", "Water", 30, "Sea Creature");
        Card card8 = new Card("FireElf", 8, "Monster", "Fire", 16, "Elf");
        Card card9 = new Card("WaterSprite", 9, "Monster", "Water", 14, "Sprite");
        Card card10 = new Card("Giant", 10, "Monster", "Normal", 35, "Giant");
        Card card11 = new Card("IceGolem", 11, "Monster", "Water", 22, "Golem");
        Card card12 = new Card("EarthElemental", 12, "Monster", "Normal", 28, "Elemental");
        Card card13 = new Card("FireImp", 13, "Monster", "Fire", 8, "Imp");
        Card card14 = new Card("AirSylph", 14, "Monster", "Normal", 17, "Sylph");
        Card card15 = new Card("WaterNymph", 15, "Monster", "Water", 13, "Nymph");
        Card card16 = new Card("FireSpell", 16, "Spell", "Fire", 0, "");
        Card card17 = new Card("WaterSpell", 17, "Spell", "Water", 0, "");
        Card card18 = new Card("RegularSpell", 18, "Spell", "Normal", 0, "");
        Card card19 = new Card("LightningSpell", 19, "Spell", "Normal", 0, "");
        Card card20 = new Card("IceSpell", 20, "Spell", "Water", 0, "");
        Card card21 = new Card("FireNovaSpell", 21, "Spell", "Fire", 0, "");
        Card card22 = new Card("MysticBlastSpell", 22, "Spell", "Normal", 0, "");
        Card card23 = new Card("MeteorShowerSpell", 23, "Spell", "Fire", 0, "");
        Card card24 = new Card("WhirlwindSpell", 24, "Spell", "Normal", 0, "");
        Card card25 = new Card("TidalWaveSpell", 25, "Spell", "Water", 0, "");
        Card card26 = new Card("FireSerpentSpell", 26, "Spell", "Fire", 0, "");
        Card card27 = new Card("EarthquakeSpell", 27, "Spell", "Normal", 0, "");
        Card card28 = new Card("AvalancheSpell", 28, "Spell", "Water", 0, "");
        Card card29 = new Card("InfernoSpell", 29, "Spell", "Fire", 0, "");
        Card card30 = new Card("ThunderstormSpell", 30, "Spell", "Normal", 0, "");
        Card card31 = new Card("FireBasilisk", 31, "Monster", "Fire", 26, "Basilisk");
        Card card32 = new Card("NormalVampire", 32, "Monster", "Normal", 20, "Vampire");
        Card card33 = new Card("WaterPirate", 33, "Monster", "Water", 15, "Pirate");
        Card card34 = new Card("FireCerberus", 34, "Monster", "Fire", 18, "Cerberus");
        Card card35 = new Card("NormalWitch", 35, "Monster", "Normal", 11, "Witch");
        Card card36 = new Card("WaterSorcerer", 36, "Monster", "Water", 10, "Sorcerer");
        Card card37 = new Card("FireWarlock", 37, "Monster", "Fire", 8, "Warlock");
        Card card38 = new Card("NormalNecromancer", 38, "Monster", "Normal", 9, "Necromancer");
        Card card39 = new Card("WaterElf", 39, "Monster", "Water", 7, "Elf");
        Card card40 = new Card("FireOrc", 40, "Monster", "Fire", 10, "Orc");
    }
}

