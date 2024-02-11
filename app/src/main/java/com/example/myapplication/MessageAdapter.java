package com.example.myapplication;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * The MessageAdapter class is responsible for displaying messages and files within a RecyclerView.
 * It extends RecyclerView.Adapter and overrides necessary methods to support different message types.
 */
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private final List<Message> messages;
    private OnItemClickListener onItemClickListener;
    private final Context context;

    public MessageAdapter(Context context, List<Message> messages) {
        this.context = context;
        this.messages = messages;
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view;
        MessageType messageType = MessageType.values()[viewType];

        switch (messageType){
            case MESSAGE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.send_message, parent, false);
                return new MessageViewHolder(view);
            case MESSAGE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.receive_message, parent, false);
                return new MessageViewHolder(view);
            case FILE_SENT:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.send_file,parent,false);
                return new FileViewHolder(view,context);
            case FILE_RECEIVED:
                view = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.receive_file,parent,false);
                return new FileViewHolder(view,context);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        Message message = messages.get(position);

        if(holder instanceof FileViewHolder){
            FileViewHolder fileHolder = (FileViewHolder) holder;
            fileHolder.fileNameTextView.setText(message.getFileName());

            // Set the file icon based on the file extension
            String extension = FileViewHolder.getExtensionFromPath(message.getFilePath());
            int iconResId = FileViewHolder.getFileIconResId(extension);

            fileHolder.fileIconImageView.setImageResource(iconResId);

            fileHolder.setFilePath(message.getFilePath());

            // Set click listener to the file view
            fileHolder.itemView.setOnClickListener(view -> {
                if (onItemClickListener != null) {
                    int adapterPosition = holder.getAdapterPosition();
                    if (adapterPosition != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(view, adapterPosition);
                    }
                }
            });
        } else {
            // It's a message ViewHolder
            MessageType messageType = MessageType.values()[holder.getItemViewType()];
            if (messageType == MessageType.MESSAGE_SENT) {
                holder.messageTextViewSender.setText(message.getContent());
                holder.timestampTextViewSender.setText(getFormattedTimestamp(message.getTimestamp()));

            } else if (messageType == MessageType.MESSAGE_RECEIVED) {
                holder.messageTextViewReceiver.setText(message.getContent());
                holder.timestampTextViewReceiver.setText(getFormattedTimestamp(message.getTimestamp()));
                holder.usernameTextViewReceiver.setText(message.getUsername());
            }
        }
    }

    @Override
    public int getItemCount() { return messages.size(); }

    @Override
    public int getItemViewType(int position) {
        Message message =  messages.get(position);
        return message.getMessageType().ordinal();
    }

    /**
     * Sets an OnItemClickListener for the adapter to handle user interaction with items in the RecyclerView.
     * @param listener - the listener that will be called when an item is clicked.
     */
    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Returns a formatted string representing the timestamp of a message.
     * @param timestamp - the timestamp in milliseconds.
     * @return - a string representing the formatted timestamp.
     */
    private String getFormattedTimestamp(long timestamp) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        return dateFormat.format(new Date(timestamp));
    }

    /**
     * An interface to be implemented by listeners for handling item clicks in the RecyclerView.
     */
    public interface OnItemClickListener {
        void onItemClick(View view, int position);
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        public TextView messageTextViewSender;
        public TextView messageTextViewReceiver;
        public TextView timestampTextViewSender;
        public TextView timestampTextViewReceiver;
        public TextView usernameTextViewSender;
        public TextView usernameTextViewReceiver;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageTextViewSender = itemView.findViewById(R.id.messageSender);
            timestampTextViewSender = itemView.findViewById(R.id.timesTampSender);
            usernameTextViewSender = itemView.findViewById(R.id.usernameSender);

            messageTextViewReceiver = itemView.findViewById(R.id.messageReceiver);
            timestampTextViewReceiver = itemView.findViewById(R.id.timesTampReceiver);
            usernameTextViewReceiver = itemView.findViewById(R.id.usernameReceiver);
        }
    }

    public static class FileViewHolder extends MessageViewHolder {
        public TextView fileNameTextView;

        public TextView fileSizeTextView;
        public ImageView fileIconImageView;
        private String filePath;
        private Context context;

        public FileViewHolder(View itemView, Context context){
            super(itemView);
            this.context = context;
            fileNameTextView = itemView.findViewById(R.id.file_name);
            fileSizeTextView = itemView.findViewById(R.id.file_size);
            fileIconImageView = itemView.findViewById(R.id.file_icon);
        }
        public void setFilePath(String filePath) {
            this.filePath = filePath;

            String extension = getExtensionFromPath(filePath);
            int iconResID = getFileIconResId(extension);

            fileIconImageView.setImageResource(iconResID);
        }

        /**
         * Returns a file extension from the given file path.
         *
         * @param filePath - the file path.
         * @return - a String representing the file extension.
         */
        static String getExtensionFromPath(String filePath){
            if (filePath == null) {
                return "";
            }
            int dotIndex = filePath.lastIndexOf(".");
            if(dotIndex == -1){
                return "";
            } else {
                return filePath.substring(dotIndex + 1);
            }
        }

        /**
         * Returns the resource ID for the corresponding file icon based on the file extension.
         *
         * @param extension - the file extension
         * @return - the resource ID of the file icon.
         */
        static int getFileIconResId(String extension){
            switch (extension){
                case "pdf":
                case "doc":
                case "docx":
                case "xls":
                case "ppt":
                case "pptx":
                    return R.drawable.document_icon;
                case "jpg":
                case "jpeg":
                case "png":
                case "bmp":
                case "gif":
                    return R.drawable.add_image;
                case "mp4":
                case "avi":
                case "mkv":
                case "mov":
                    return R.drawable.video_icon;
                case "mp3":
                case "wav":
                case "m4a":
                    return R.drawable.audio_icon;
                default:
                    return R.drawable.ic_file_black;
            }
        }
    }
}



