package com.planet_ink.coffee_mud.Abilities.Misc;

import com.planet_ink.coffee_mud.Abilities.StdAbility;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Prisoner extends StdAbility
{
	public String ID() { return "Prisoner"; }
	public String name(){ return "Prisoner";}
	public String displayText(){ return "(Prisoner's Geas)";}
	protected int canAffectCode(){return CAN_MOBS;}
	protected int canTargetCode(){return CAN_MOBS;}
	public long flags(){return Ability.FLAG_BINDING;}

	public boolean okMessage(Environmental myHost, CMMsg msg)
	{
		if((affected instanceof MOB)&&(msg.amISource((MOB)affected)))
			if(msg.sourceMinor()==CMMsg.TYP_RECALL)
			{
				if((msg.source()!=null)&&(msg.source().location()!=null))
					msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to recall, but a geas prevents <S-HIM-HER>.");
				return false;
			}
			else
			if(msg.sourceMinor()==CMMsg.TYP_FLEE)
			{
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to flee, but a geas prevents <S-HIM-HER>.");
				return false;
			}
			else
			if((msg.tool()!=null)&&(msg.tool() instanceof Ability)
			   &&(msg.targetMinor()==CMMsg.TYP_LEAVE))
			{
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to escape parole, but a geas prevents <S-HIM-HER>.");
				return false;
			}
			else
			if((msg.targetMinor()==CMMsg.TYP_ENTER)
			   &&(msg.target()!=null)
			   &&(msg.target() instanceof Room)
			   &&(msg.source().location()!=null)
			   &&(!msg.source().location().getArea().name().equals(((Room)msg.target()).getArea().name())))
			{
				msg.source().location().show(msg.source(),null,CMMsg.MSG_OK_ACTION,"<S-NAME> attempt(s) to escape parole, but a geas prevents <S-HIM-HER>.");
				return false;
			}
		return super.okMessage(myHost,msg);
	}

	public void unInvoke()
	{
		// undo the affects of this spell
		if((affected==null)||(!(affected instanceof MOB)))
			return;
		MOB mob=(MOB)affected;

		super.unInvoke();

		if(canBeUninvoked())
			mob.tell("Your sentence has been served.");
	}
}
