package com.treem.treem.widget;

/**
 * Date: 5/24/16.
 */
class HexagonGridPosition {

	int x;
	int y;

	public HexagonGridPosition(int x, int y) {
		this.x = x;
		this.y = y;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) {
			return true;
		}

		if (o == null || getClass() != o.getClass()) {
			return false;
		}

		HexagonGridPosition position = (HexagonGridPosition) o;

		return x == position.x && y == position.y;

	}

	@Override
	public int hashCode() {
		int result = x;
		result = 31 * result + y;
		return result;
	}
}
