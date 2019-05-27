#if (${PACKAGE_NAME} && ${PACKAGE_NAME} != "")package ${PACKAGE_NAME};#end

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import java.util.List;

#parse("File Header.java")
public class ${NAME} extends RecyclerView.Adapter<${VIEWHOLDER_CLASS}> {
    private final Context context;
    private List<${ITEM_CLASS}> items;

    public ${NAME}(List<${ITEM_CLASS}> items, Context context) {
        this.items = items;
        this.context = context;
    }

    @Override
    public ${VIEWHOLDER_CLASS} onCreateViewHolder(ViewGroup parent,
                                             int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.${LAYOUT_RES_ID}, parent, false);
        return new ${VIEWHOLDER_CLASS}(v);
    }

    @Override
    public void onBindViewHolder(${VIEWHOLDER_CLASS} holder, int position) {
        ${ITEM_CLASS} item = items.get(position);
        holder.set(item);
    }

    @Override
    public int getItemCount() {
        if (items == null){
            return 0;
        }
        return items.size();
    }

    public class ${VIEWHOLDER_CLASS} extends RecyclerView.ViewHolder {

        public ${VIEWHOLDER_CLASS}(View itemView) {
            super(itemView);
        }

        public void set(${ITEM_CLASS} item) {
            //UI setting code
        }
    }
 }