package io.github.kraowx.shibbyapp.ui.playlists.itemtouch;

public interface ItemTouchHelperAdapter
{
	boolean onItemMove(int fromPosition, int toPosition);
	
	void onItemReleased();
}
