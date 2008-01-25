package com.planet_ink.coffee_mud.Abilities.Traps;
import com.planet_ink.coffee_mud.core.interfaces.*;
import com.planet_ink.coffee_mud.core.*;
import com.planet_ink.coffee_mud.Abilities.interfaces.*;
import com.planet_ink.coffee_mud.Areas.interfaces.*;
import com.planet_ink.coffee_mud.Behaviors.interfaces.*;
import com.planet_ink.coffee_mud.CharClasses.interfaces.*;
import com.planet_ink.coffee_mud.Commands.interfaces.*;
import com.planet_ink.coffee_mud.Common.interfaces.*;
import com.planet_ink.coffee_mud.Exits.interfaces.*;
import com.planet_ink.coffee_mud.Items.interfaces.*;
import com.planet_ink.coffee_mud.Locales.interfaces.*;
import com.planet_ink.coffee_mud.MOBS.interfaces.*;
import com.planet_ink.coffee_mud.Races.interfaces.*;

import java.util.*;

/* 
   Copyright 2000-2007 Bo Zimmerman

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
public class Trap_FloodRoom extends StdTrap
{
	public String ID() { return "Trap_FloodRoom"; }
	public String name(){ return "flood room";}
	protected int canAffectCode(){return Ability.CAN_ROOMS;}
	protected int canTargetCode(){return 0;}
	protected int trapLevel(){return 29;}
	public String requiresToSet(){return "100 pounds of stone, 10 water containers";}
	public int baseRejuvTime(int level){ return 16;}

	protected int numWaterskins(MOB mob)
	{
		if(mob==null) return 0;
		if(mob.location()==null) return 0;
		int num=0;
		for(int i=0;i<mob.location().numItems();i++)
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof Drink)&&(((Drink)I).containsDrink()))
				num++;
		}
		return num;
	}

    protected void killWaterskins(MOB mob)
	{
		if(mob==null) return;
		if(mob.location()==null) return;
		int num=10;
		int i=0;
		while((num>0)&&(i<mob.location().numItems()))
		{
			Item I=mob.location().fetchItem(i);
			if((I instanceof Drink)&&(((Drink)I).containsDrink()))
			{
				if(I instanceof RawMaterial)
				{
					i--;
					I.destroy();
				}
				else
					((Drink)I).setLiquidRemaining(0);
				if((--num)<=0) break;
			}
			i++;
		}
	}


	public Trap setTrap(MOB mob, Environmental E, int trapBonus, int qualifyingClassLevel, boolean perm)
	{
		if(E==null) return null;
		Item I=null;
		if(mob!=null)
		{
			I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if(I!=null)	super.destroyResources(mob.location(),I.material(),100);
			killWaterskins(mob);
		}
		return super.setTrap(mob,E,trapBonus,qualifyingClassLevel,perm);
	}


	public boolean canSetTrapOn(MOB mob, Environmental E)
	{
		if(!super.canSetTrapOn(mob,E)) return false;
		if(mob!=null)
		{
			Item I=findMostOfMaterial(mob.location(),RawMaterial.MATERIAL_ROCK);
			if((I==null)
			||(super.findNumberOfResource(mob.location(),I.material())<100))
			{
				mob.tell("You'll need to set down at least 100 pounds of stone first.");
				return false;
			}
			if(numWaterskins(mob)<=10)
			{
				mob.tell("You'll need to set down at least 10 water containers first.");
				return false;
			}
		}
		if(E instanceof Room)
		{
			Room R=(Room)E;
			if((R.domainType()&Room.INDOORS)==0)
			{
				if(mob!=null)
					mob.tell("You can only set this trap indoors.");
				return false;
			}
		}
		return true;
	}

	public void affectEnvStats(Environmental affected, EnvStats affectableStats)
	{
		super.affectEnvStats(affected,affectableStats);
		if(sprung)
		{
			if((!disabled)&&((tickDown>2)&&(tickDown<13)))
			{
				affectableStats.setSensesMask(affectableStats.sensesMask()|EnvStats.CAN_NOT_BREATHE);
				affectableStats.setDisposition(affectableStats.disposition()|EnvStats.IS_SWIMMING);
			}
		}
		else
			disabled=false;
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((sprung)
		&&(affected!=null)
		&&(!disabled())
		&&(tickDown>=0))
		{
			if(((msg.targetMinor()==CMMsg.TYP_LEAVE)
				||(msg.targetMinor()==CMMsg.TYP_FLEE))
			   &&(msg.amITarget(affected)))
			{
				msg.source().tell("The exits are blocked! You can't get out!");
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			   &&(msg.amITarget(affected)))
			{
				msg.source().tell("The entry to that room is blocked!");
				return false;
			}
		}
		return super.okMessage(myHost,msg);
	}

	public boolean tick(Tickable ticking, int tickID)
	{
		if((tickID==Tickable.TICKID_TRAP_RESET)&&(getReset()>0))
		{
			if((sprung)
			&&(affected!=null)
			&&(affected instanceof Room)
			&&(!disabled())
			&&(tickDown>=0))
			{
				Room R=(Room)affected;
				if(tickDown>13)
				{
					R.showHappens(CMMsg.MSG_OK_VISUAL,"Water is filling up the room!");
					CMLib.utensils().extinguish(invoker(),R,true);
					R.recoverEnvStats();
					R.recoverRoomStats();
				}
				else
				if(tickDown>2)
				{
					CMLib.utensils().extinguish(invoker(),R,true);
					R.recoverEnvStats();
					R.recoverRoomStats();
				}
				else
				{
					R.recoverEnvStats();
					R.recoverRoomStats();
					R.showHappens(CMMsg.MSG_OK_VISUAL,"The water is draining away...");
				}
			}
		}
		return super.tick(ticking,tickID);
	}

	public void disable(){
		super.disable();
		if((affected!=null)&&(affected instanceof Room))
		{
			((Room)affected).recoverEnvStats();
			((Room)affected).recoverRoomStats();
		}
	}

	public void spring(MOB target)
	{
		if((target!=invoker())&&(target.location()!=null))
		{
			if((CMLib.dice().rollPercentage()<=target.charStats().getSave(CharStats.STAT_SAVE_TRAPS))
			||(invoker().getGroupMembers(new HashSet()).contains(target)))
				target.location().show(target,null,null,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> avoid(s) setting off a trap!");
			else
			if(target.location().show(target,target,this,CMMsg.MASK_ALWAYS|CMMsg.MSG_NOISE,"<S-NAME> trigger(s) a trap!"))
			{
				super.spring(target);
				target.location().showHappens(CMMsg.MSG_OK_VISUAL,"The exits are blocked off! Water starts pouring in!");
			}
		}
	}
}
