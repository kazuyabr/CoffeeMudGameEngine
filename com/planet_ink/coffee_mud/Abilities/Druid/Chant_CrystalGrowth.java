package com.planet_ink.coffee_mud.Abilities.Druid;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.io.*;
import java.util.*;

public class Chant_CrystalGrowth extends Chant
{
	public String ID() { return "Chant_CrystalGrowth"; }
	public String name(){ return "Crystal Growth";}
	protected int canAffectCode(){return 0;}
	protected int canTargetCode(){return 0;}
	protected int overrideMana(){return 50;}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		if(mob.location().domainType()!=Room.DOMAIN_INDOORS_CAVE)
		{
			mob.tell("This magic will not work here.");
			return false;
		}
		int material=EnvResource.RESOURCE_CRYSTAL;
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		// now see if it worked
		boolean success=profficiencyCheck(mob,0,auto);
		if(success)
		{
			FullMsg msg=new FullMsg(mob,null,this,affectType(auto),auto?"":"^S<S-NAME> chant(s) to the cave walls.^?");
			if(mob.location().okMessage(mob,msg))
			{
				mob.location().send(mob,msg);

				Item building=null;
				Ability A=null;
				switch(Dice.roll(1,10,0))
				{
				case 1:
				case 2:
				case 3:
				case 4:
					A=CMClass.getAbility("Blacksmithing");
					break;
				case 5:
				case 6:
				case 7:
					A=CMClass.getAbility("Armorsmithing");
					break;
				case 8:
				case 9:
				case 10:
					A=CMClass.getAbility("Weaponsmithing");
					break;
				}
				if(A!=null)
				{
					while((building==null)||(building.name().endsWith(" bundle")))
					{
						Vector V=new Vector();
						V.addElement(new Integer(material));
						A.invoke(mob,V,A,true);
						if((V.size()>0)&&(V.lastElement() instanceof Item))
							building=(Item)V.lastElement();
						else
							break;
					}
				}
				if(building==null)
				{
					mob.tell("The chant failed for some reason...");
					return false;
				}
				Ability A2=CMClass.getAbility("Chant_Brittle");
				if(A2!=null)
					building.addNonUninvokableEffect(A2);

				building.recoverEnvStats();
				building.text();
				building.recoverEnvStats();

				mob.location().addItemRefuse(building,Item.REFUSE_RESOURCE);
				mob.location().showHappens(CMMsg.MSG_OK_ACTION,"a tiny crystal fragment drops out of the stone, swells and grows, forming into "+building.name()+".");
				mob.location().recoverEnvStats();
			}
		}
		else
			return beneficialWordsFizzle(mob,null,"<S-NAME> chant(s) to the walls, but nothing happens.");

		// return whether it worked
		return success;
	}
}