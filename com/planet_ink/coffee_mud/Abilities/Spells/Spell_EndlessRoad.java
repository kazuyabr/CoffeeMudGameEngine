package com.planet_ink.coffee_mud.Abilities.Spells;

import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Spell_EndlessRoad extends Spell
{
	public String ID() { return "Spell_EndlessRoad"; }
	public String name(){return "Endless Road";}
	public String displayText(){return "(Endless Road)";}
	public int quality(){return MALICIOUS;};
	protected int canAffectCode(){return CAN_MOBS;}
	public int classificationCode(){	return Ability.SPELL|Ability.DOMAIN_ILLUSION;}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();
		if(canBeUninvoked())
			mob.tell("You feel like you are finally getting somewhere.");
		CommonMsgs.stand(mob,true);
	}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return super.okMessage(myHost,msg);
		MOB mob=(MOB)affected;
		if(msg.amISource(mob)
		   &&(mob.location()!=null)
		   &&(msg.targetMinor()==CMMsg.TYP_ENTER)
		   &&(msg.target()!=null)
		   &&(msg.target() instanceof Room)
		   &&(msg.target()!=mob.location()))
		{
			msg.modify(msg.source(),
					   mob.location(),
					   msg.tool(),
					   msg.sourceCode(),
					   msg.sourceMessage(),
					   msg.targetCode(),
					   msg.targetMessage(),
					   msg.othersCode(),
					   msg.othersMessage());
		}

		return super.okMessage(myHost,msg);
	}

	public boolean invoke(MOB mob, Vector commands, Environmental givenTarget, boolean auto)
	{
		MOB target=this.getTarget(mob,commands,givenTarget);
		if(target==null) return false;

		// the invoke method for spells receives as
		// parameters the invoker, and the REMAINING
		// command line parameters, divided into words,
		// and added as String objects to a vector.
		if(!super.invoke(mob,commands,givenTarget,auto))
			return false;

		boolean success=profficiencyCheck(mob,0,auto);

		if(success)
		{
			// it worked, so build a copy of this ability,
			// and add it to the affects list of the
			// affected MOB.  Then tell everyone else
			// what happened.
			invoker=mob;
			FullMsg msg=new FullMsg(mob,target,this,affectType(auto),auto?"":"^S<S-NAME> incant(s) to <T-NAMESELF>!^?");
			FullMsg msg2=new FullMsg(mob,target,this,CMMsg.MSK_CAST_MALICIOUS_VERBAL|CMMsg.TYP_MIND|(auto?CMMsg.MASK_GENERAL:0),null);
			if((mob.location().okMessage(mob,msg))&&(mob.location().okMessage(mob,msg2)))
			{
				mob.location().send(mob,msg);
				mob.location().send(mob,msg2);
				if((msg.value()<=0)&&(msg2.value()<=0))
				{
					success=maliciousAffect(mob,target,0,-1);
					if(success)
						if(target.location()==mob.location())
							target.location().show(target,null,CMMsg.MSG_OK_ACTION,"<S-NAME> seem(s) lost!");
				}
			}
		}
		else
			return maliciousFizzle(mob,target,"<S-NAME> incant(s) to <T-NAMESELF>, but the spell fizzles");

		// return whether it worked
		return success;
	}
}
