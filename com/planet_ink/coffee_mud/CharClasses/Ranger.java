package com.planet_ink.coffee_mud.CharClasses;

import java.util.*;
import com.planet_ink.coffee_mud.utils.*;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;

public class Ranger extends StdCharClass
{
	private static boolean abilitiesLoaded=false;
	
	public Ranger()
	{
		super();
		myID=this.getClass().getName().substring(this.getClass().getName().lastIndexOf('.')+1);
		maxHitPointsPerLevel=22;
		maxStat[CharStats.STRENGTH]=22;
		maxStat[CharStats.DEXTERITY]=22;
		bonusPracLevel=0;
		manaMultiplier=10;
		attackAttribute=CharStats.STRENGTH;
		bonusAttackLevel=2;
		name=myID;
		practicesAtFirstLevel=3;
		trainsAtFirstLevel=4;
		damageBonusPerLevel=1;
		if(!abilitiesLoaded)
		{
			abilitiesLoaded=true;
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Write",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Axe",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_BluntWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_EdgedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_FlailedWeapon",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Hammer",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Natural",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Polearm",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Ranged",false);
			CMAble.addCharAbilityMapping(ID(),1,"Specialization_Sword",true);
			CMAble.addCharAbilityMapping(ID(),1,"Ranger_Track",true);
			CMAble.addCharAbilityMapping(ID(),1,"Skill_Recall",true);
			CMAble.addCharAbilityMapping(ID(),2,"Fighter_Rescue",true);
			CMAble.addCharAbilityMapping(ID(),3,"Skill_Parry",true);
			CMAble.addCharAbilityMapping(ID(),4,"Skill_Bash",true);
			CMAble.addCharAbilityMapping(ID(),5,"Skill_WandUse",false);
			CMAble.addCharAbilityMapping(ID(),6,"Skill_Revoke",false);
			CMAble.addCharAbilityMapping(ID(),7,"Skill_Dodge",true);
			CMAble.addCharAbilityMapping(ID(),8,"Skill_Disarm",true);
			CMAble.addCharAbilityMapping(ID(),9,"Skill_Attack2",true);
			CMAble.addCharAbilityMapping(ID(),11,"Skill_Dirt",true);
			CMAble.addCharAbilityMapping(ID(),12,"Fighter_BlindFighting",true);
			CMAble.addCharAbilityMapping(ID(),15,"Skill_Climb",false);
			CMAble.addCharAbilityMapping(ID(),17,"Skill_Trip",true);
			CMAble.addCharAbilityMapping(ID(),20,"Skill_Attack3",true);	
			
			// qualify for all spells
			Mage m=new Mage(); // make sure a mage is available
			for(int level=1;level<22;level++)
			{
				Vector V=CMAble.getLevelListings(m.ID(),level);
				for(int v=0;v<V.size();v++)
				{
					String spell=(String)V.elementAt(v);
					if(spell.startsWith("Spell_"))
						CMAble.addCharAbilityMapping(ID(),level+4,spell,false);
				}
			}
			
			CMAble.addCharAbilityMapping(ID(),5,"Spell_ReadMagic",true);
			CMAble.addCharAbilityMapping(ID(),6,"Spell_Light",true);
		}
	}

	public boolean playerSelectable()
	{
		return true;
	}

	public boolean qualifiesForThisClass(MOB mob)
	{
		if(mob.baseCharStats().getStrength()<=8)
			return false;

		if(mob.baseCharStats().getIntelligence()<=8)
			return false;

		if(!(mob.charStats().getMyRace().ID().equals("Human"))
		&& !(mob.charStats().getMyRace().ID().equals("Elf"))
		&& !(mob.charStats().getMyRace().ID().equals("HalfElf")))
			return(false);


		return true;
	}

	public void outfit(MOB mob)
	{
		Weapon w=(Weapon)CMClass.getWeapon("Shortsword");
		if(mob.fetchInventory(w.ID())==null)
		{
			mob.addInventory(w);
			if(!mob.amWearingSomethingHere(Item.WIELD))
				w.wearAt(Item.WIELD);
		}
	}
	public void newCharacter(MOB mob, boolean isBorrowedClass)
	{
		super.newCharacter(mob, isBorrowedClass);
		for(int a=0;a<CMClass.abilities.size();a++)
		{
			Ability A=(Ability)CMClass.abilities.elementAt(a);
			if((A.qualifyingLevel(mob)>0)&&(CMAble.getDefaultGain(ID(),A.ID())))
				this.giveMobAbility(mob,A,isBorrowedClass);
		}
		if(!mob.isMonster())
			outfit(mob);
	}

	public void level(MOB mob)
	{
		super.level(mob);
	}
}
