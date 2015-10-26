package messenger.profile;

public interface Scope {
	public boolean isInScope(Profile thisProf, Profile otherProf);
}
