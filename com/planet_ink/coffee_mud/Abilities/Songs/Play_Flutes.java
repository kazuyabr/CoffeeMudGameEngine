package com.planet_ink.coffee_mud.Abilities.Songs;
import com.planet_ink.coffee_mud.interfaces.*;
import com.planet_ink.coffee_mud.common.*;
import com.planet_ink.coffee_mud.utils.*;
import java.util.*;

public class Play_Flutes extends Play_Instrument
{
	public String ID() { return "Play_Flutes"; }
	public String name(){ return "Flutes";}
	protected int requiredInstrumentType(){return MusicalInstrument.TYPE_FLUTES;}
	public String mimicSpell(){return "Chant_CharmAnimal";}
	private static Ability theSpell=null;
	protected Ability getSpell()
	{
		if(theSpell!=null) return theSpell;
		if(mimicSpell().length()==0) return null;
		theSpell=CMClass.getAbility(mimicSpell());
		return theSpell;
	}
}
