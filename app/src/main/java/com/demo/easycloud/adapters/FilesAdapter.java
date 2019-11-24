package com.demo.easycloud.adapters;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.demo.easycloud.R;
import com.demo.easycloud.ViewFolderActivity;
import com.demo.easycloud.ViewImageActivity;
import com.demo.easycloud.ViewVideoActivity;
import com.demo.easycloud.models.File;

import java.util.ArrayList;
import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class FilesAdapter extends RecyclerView.Adapter<FilesAdapter.FileHolder> implements Filterable {

    private List<File> fileList;
    private List<File> completFileList;

    public FilesAdapter(List<File> fileList) {
        this.fileList = fileList;
        this.completFileList = fileList;
    }

    @Override
    public FileHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.dashboard_list_row, parent, false);

        return new FileHolder(itemView);
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                String charString = charSequence.toString();
                if (charString.isEmpty()) {
                    fileList = completFileList;
                } else {
                    List<File> filteredList = new ArrayList<>();
                    for (File file : completFileList) {
                        if (file.getTitle().toLowerCase().contains(charString.toLowerCase())) {
                            filteredList.add(file);
                        }
                    }
                    fileList = filteredList;
                }

                FilterResults filterResults = new FilterResults();
                filterResults.values = fileList;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                fileList = (ArrayList<File>) filterResults.values;

                notifyDataSetChanged();
            }
        };
    }

    @Override
    public void onBindViewHolder(FileHolder holder, int position) {
        File file = fileList.get(position);
        holder.title.setText(file.getTitle());
//        holder.fileType.setImageDrawable(file.getFileType());

        Log.d("FileType", file.getFileType());

        holder.uploadedOn.setText(file.getUpdatedOn());

        if(file.getFileType().equals("Folder")) {
            holder.fileType.setImageResource(R.drawable.ic_action_folder);
        }
    }

    @Override
    public int getItemCount() {
        return fileList.size();
    }

    public class FileHolder extends RecyclerView.ViewHolder {
        public TextView title, uploadedOn;

        public ImageView fileType;

        public FileHolder(View view) {
            super(view);
            title = (TextView) view.findViewById(R.id.title);
            fileType = (ImageView) view.findViewById(R.id.fileType);
            uploadedOn = (TextView) view.findViewById(R.id.uploadedOn);

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = getAdapterPosition();// to gr

                    Log.d("FileTypes", fileList.get(position).getFileType());

                    if(fileList.get(position).getFileType().equals("Folder")) {
                        Intent i = new Intent(v.getContext(), ViewFolderActivity.class);

                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("path", fileList.get(position).getPath());
                        dataBundle.putString("imageTitle", fileList.get(position).getTitle());
                        i.putExtras(dataBundle);

                        v.getContext().startActivity(i);
                    } else if(fileList.get(position).getFileType().equals("image/jpeg") || fileList.get(position).getFileType().equals("image/png")) {
                        Intent i = new Intent(v.getContext(), ViewImageActivity.class);

                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("imageTitle", fileList.get(position).getTitle());
                        dataBundle.putString("extension", fileList.get(position).getFileType().substring(fileList.get(position).getFileType().lastIndexOf("/") + 1));
                        dataBundle.putString("imageUrl", fileList.get(position).getUri());
                        dataBundle.putString("imagePath", fileList.get(position).getPath());
                        i.putExtras(dataBundle);

                        v.getContext().startActivity(i);
                    } else {
                        Intent i = new Intent(v.getContext(), ViewVideoActivity.class);

                        Bundle dataBundle = new Bundle();
                        dataBundle.putString("imageTitle", fileList.get(position).getTitle());
                        dataBundle.putString("extension", fileList.get(position).getFileType().substring(fileList.get(position).getFileType().lastIndexOf("/") + 1));
                        dataBundle.putString("imageUrl", fileList.get(position).getUri());
                        dataBundle.putString("imagePath", fileList.get(position).getPath());
                        i.putExtras(dataBundle);

                        v.getContext().startActivity(i);
                    }
                }
            });
        }
    }
}