package com.treem.treem.helpers;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;

import com.treem.treem.R;

/**
 * Date: 7/4/16.
 */
public final class DialogHelper {

	/**
	 * Show alert dialog with specified title and message
	 *
	 * @param context Base context
	 * @param title   Dialog title
	 * @param message Dialog message
	 */
	public static void showAlert(@NonNull Context context, @Nullable String title, @NonNull String message) {
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.ok, null)
				.show();
	}

	/**
	 * Show alert dialog with specified title resource and message resource
	 *
	 * @param context      Base context
	 * @param titleResId   Resource id of dialog title
	 * @param messageResId Resource id of dialog message
	 */
	public static void showAlert(@NonNull Context context, @StringRes int titleResId, @StringRes int messageResId) {
		showAlert(context, context.getString(titleResId), context.getString(messageResId));
	}

	/**
	 * Show confirmation dialog with specified title and message
	 *
	 * @param context                     Base context
	 * @param title                       Dialog title
	 * @param message                     Dialog message
	 * @param positiveButtonClickListener Callback of positive button
	 */
	public static void showConfirmation(@NonNull Context context,
										@Nullable String title,
										@NonNull String message,
										@NonNull DialogInterface.OnClickListener positiveButtonClickListener) {
		new AlertDialog.Builder(context)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.ok, positiveButtonClickListener)
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	/**
	 * Show confirmation dialog with specified title resource and message resource
	 *
	 * @param context                       Base context
	 * @param titleResId                    Resource id of dialog title
	 * @param messageResId                  Resource id of dialog message
	 * @param onPositiveButtonClickListener Callback of positive button
	 */
	public static void showConfirmation(@NonNull Context context,
										@StringRes int titleResId,
										@StringRes int messageResId,
										@NonNull DialogInterface.OnClickListener onPositiveButtonClickListener) {
		showConfirmation(context, context.getString(titleResId), context.getString(messageResId), onPositiveButtonClickListener);
	}
}
