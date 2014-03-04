public class Coord{
	public int x;
	public int y;

	Coord(int x, int  y){
		this.x = x;
		this.y = y;
	}

    @Override public boolean equals(Object other) {
        boolean result = false;
        if (other instanceof Coord) {
            Coord that = (Coord) other;
            result = (this.x == that.x && this.y == that.y);
        }
        return result;
    }

    @Override public int hashCode() {
        return (41 * (41 + this.x + this.y));
    }
}