public class TLelement {
	private Integer e;
	private int timeslot;

	public TLelement(Integer e, int timeslot) {
		this.e = e;
		this.timeslot = timeslot;
	}

	public Integer getE() {
		return e;
	}

	public int getTimeslot() {
		return timeslot;
	}

	public void setE(Integer e) {
		this.e = e;
	}

	public void setTimeslot(int timeslot) {
		this.timeslot = timeslot;
	}

	public String toString() {
		return this.e + " in timeslot " + this.timeslot + "\n";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((e == null) ? 0 : e.hashCode());
		result = prime * result + timeslot;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TLelement other = (TLelement) obj;
		if (e == null) {
			if (other.e != null)
				return false;
		} else if (!e.equals(other.e))
			return false;
		if (timeslot != other.timeslot)
			return false;
		return true;
	}
}
