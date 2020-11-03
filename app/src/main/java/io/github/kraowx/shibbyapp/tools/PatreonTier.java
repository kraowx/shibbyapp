package io.github.kraowx.shibbyapp.tools;

public class PatreonTier
{
	public static final int USER = -1;
	public static final int FREE = 0;
	public static final int DRIFTING = 1;
	public static final int HYPNOSUB = 2;
	public static final int HYPNOSLAVE = 3;
	public static final int HYPNOSLUT = 4;
	public static final int DEVOTED_PET = 5;
	public static final int WORSHIPFUL_SUBJECT = 6;
	
	private int tier;
	
	public PatreonTier(int tier)
	{
		this.tier = tier;
	}
	
	public static PatreonTier fromString(String str)
	{
		switch (str)
		{
			case "user":
				return new PatreonTier(USER);
			case "drifting":
				return new PatreonTier(DRIFTING);
			case "hypnosub":
				return new PatreonTier(HYPNOSUB);
			case "hypnoslave":
				return new PatreonTier(HYPNOSLAVE);
			case "hypnoslut":
				return new PatreonTier(HYPNOSLUT);
			case "devoted pet":
				return new PatreonTier(DEVOTED_PET);
			case "worshipful subject":
				return new PatreonTier(WORSHIPFUL_SUBJECT);
			default:
				return new PatreonTier(FREE);
		}
	}
	
	public int getTier()
	{
		return tier;
	}
	
	public boolean greaterThan(PatreonTier otherTier)
	{
		return otherTier != null && tier > otherTier.tier;
	}
	
	public boolean greaterThanEquals(PatreonTier otherTier)
	{
		return otherTier != null && tier >= otherTier.tier;
	}
	
	public boolean lessThan(PatreonTier otherTier)
	{
		return otherTier != null && tier < otherTier.tier;
	}
	
	public boolean lessThanEquals(PatreonTier otherTier)
	{
		return otherTier != null && tier <= otherTier.tier;
	}
	
	public boolean equals(Object obj)
	{
		if (obj == null || getClass() != obj.getClass())
		{
			return false;
		}
		PatreonTier otherTier = (PatreonTier)obj;
		return this.tier == otherTier.tier;
	}
}
