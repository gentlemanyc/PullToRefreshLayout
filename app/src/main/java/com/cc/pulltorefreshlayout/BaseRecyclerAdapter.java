package com.cc.pulltorefreshlayout;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import static android.view.View.GONE;


/**
 * Created by yuanc on 2016/8/16.
 */
public abstract class BaseRecyclerAdapter<Holder extends RecyclerView.ViewHolder, Data> extends RecyclerView.Adapter<Holder> {

    List<Data> list;
    private View emptyView;

    public BaseRecyclerAdapter(List<Data> list) {
        this.list = list;
    }

    public BaseRecyclerAdapter(List<Data> list, View emptyView) {
        this.list = list;
        this.emptyView = emptyView;
        if (emptyView != null)
            emptyView.setVisibility(list == null || list.size() == 0 ? View.VISIBLE : GONE);
    }

    public BaseRecyclerAdapter() {
        this.list = new ArrayList<>();
    }

    public void refresh(List<Data> list) {
        this.list = list;
        notifyDataSetChanged();
        if (emptyView != null) {
            emptyView.setVisibility(list == null || list.size() == 0 ? View.VISIBLE : GONE);
        }
    }

    public void loadMore(List<Data> list) {
        this.list.addAll(list);
        notifyDataSetChanged();
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    public void clear() {
        if (list != null) {
            list.clear();
            notifyDataSetChanged();
        }
        if (emptyView != null) {
            emptyView.setVisibility(View.VISIBLE);
        }
    }


    public List<Data> getList() {
        return list;
    }

    public Data getItem(int position) {
        return list.get(position);
    }

    @Override
    public Holder onCreateViewHolder(ViewGroup parent, int viewType) {
        parent.setClickable(true);
        View itemView = null;
        if (getItemLayout() != 0)
            itemView = LayoutInflater.from(parent.getContext()).inflate(getItemLayout(), parent, false);
//        itemView.setBackgroundResource(R.drawable.item_selector);
        return getHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final Holder holder, final int position) {
        handleClickListener(holder, position);
        if (position < list.size()) {
            bindView(holder, list.get(position), position);
        } else {
            bindView(holder, null, position);
        }
    }

    protected void handleClickListener(final Holder holder, final int position) {
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (getOnItemClickListener() != null) {
                    onItemClickListener.onItemClick(holder.itemView, position);
                }
            }
        });
    }

    protected abstract int getItemLayout();

    protected abstract void bindView(Holder holder, Data data, int position);

    protected abstract Holder getHolder(View itemView);

    @Override
    public int getItemCount() {
        return list.size();
    }

    OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public OnItemClickListener getOnItemClickListener() {
        return onItemClickListener;
    }

    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }
}
