package it.uninsubria.pdm.audiotodolist.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.io.Serializable;

import it.uninsubria.pdm.audiotodolist.R;

public class TagsDialogFragment extends DialogFragment {

    public interface TagsDialogFragmentListener extends Serializable {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    TagsDialogFragmentListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            listener = TagsDialogFragmentArgs.fromBundle(getArguments()).getListener();
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement TagsDialogFragmentListener interface");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        MaterialAlertDialogBuilder builder = new MaterialAlertDialogBuilder(getActivity());
        builder.setTitle(R.string.add_tags_title)
                .setMessage(R.string.add_tags_body)
                .setView(R.layout.tag_dialog)
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    TagsDialogFragment.this.getDialog().cancel();
                    listener.onDialogNegativeClick(null);
                })
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    listener.onDialogPositiveClick(this);
                });
        return builder.create();
    }
}
