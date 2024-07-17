package io.noties.markwon.ext.tables;

import static android.view.View.NO_ID;

import android.text.Spanned;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

abstract class TableRowsScheduler {

    static void schedule(@NonNull final TextView view, @IdRes int heightResId) {
        final Object[] spans = extract(view);
        if (spans != null
                && spans.length > 0) {

            if (view.getTag(R.id.markwon_tables_scheduler) == null) {
                final View.OnAttachStateChangeListener listener = new View.OnAttachStateChangeListener() {
                    @Override
                    public void onViewAttachedToWindow(View v) {

                    }

                    @Override
                    public void onViewDetachedFromWindow(View v) {
                        unschedule(view);
                        view.removeOnAttachStateChangeListener(this);
                        view.setTag(R.id.markwon_tables_scheduler, null);
                    }
                };
                view.addOnAttachStateChangeListener(listener);
                view.setTag(R.id.markwon_tables_scheduler, listener);
            }

            final TableRowSpan.Invalidator invalidator = new TableRowSpan.Invalidator() {

                // @since 4.1.0
                // let's stack-up invalidation calls (so invalidation happens,
                // but not with each table-row-span draw call)
                final Runnable runnable = new Runnable() {
                    @Override
                    public void run() {
                        view.setText(view.getText());
                    }
                };

                @Override
                public void invalidate(int rowNumber, int height) {
                    // @since 4.1.0 post invalidation (combine multiple calls)
                    if (rowNumber != -1 && heightResId != NO_ID) {
                        Object heights = view.getTag(heightResId);
                        if (heights == null || heights instanceof ArrayList == false) {
                            heights = new ArrayList<Integer>();
                            ((ArrayList) heights).add(height);
                        } else {
                            int size = ((ArrayList<Integer>) heights).size();
                            if (rowNumber + 1 <= size) {
                                ((ArrayList) heights).set(rowNumber, height);
                            } else {
                                ((ArrayList) heights).add(height);
                            }
                        }
                        view.setTag(heightResId, heights);

                    }
                    view.removeCallbacks(runnable);
                    view.post(runnable);
                }

                @Override
                public void cancel() {
                    view.removeCallbacks(runnable);
                }
            };

            for (Object span : spans) {
                ((TableRowSpan) span).invalidator(invalidator);
            }
        }
    }

    static void unschedule(@NonNull TextView view) {
        final Object[] spans = extract(view);
        if (spans != null
                && spans.length > 0) {
            for (Object span : spans) {
                ((TableRowSpan) span).invalidator(null);
            }
        }
    }

    @Nullable
    private static Object[] extract(@NonNull TextView view) {
        final Object[] out;
        final CharSequence text = view.getText();
        if (!TextUtils.isEmpty(text) && text instanceof Spanned) {
            out = ((Spanned) text).getSpans(0, text.length(), TableRowSpan.class);
        } else {
            out = null;
        }
        return out;
    }

    private TableRowsScheduler() {
    }
}
