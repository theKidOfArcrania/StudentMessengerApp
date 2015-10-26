package messenger.profile;

public enum StandardScope implements Scope {
	Private {

		@Override
		public boolean isInScope(Profile thisProf, Profile otherProf) {
			return thisProf.equals(otherProf);
		}

	},
	Public {

		@Override
		public boolean isInScope(Profile thisProf, Profile otherProf) {
			return true;
		}

	}
}
