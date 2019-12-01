package io.github.kraowx.shibbyapp.ui.favorites;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import io.github.kraowx.shibbyapp.MainActivity;
import io.github.kraowx.shibbyapp.R;

public class FavoritesFragment extends Fragment
{
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState)
    {
        View root = inflater.inflate(R.layout.fragment_favorites, container, false);
        FloatingActionButton fabAddPlaylist =
                ((MainActivity)getActivity()).findViewById(R.id.fabAddPlaylist);
        fabAddPlaylist.hide();
        return root;
    }
}