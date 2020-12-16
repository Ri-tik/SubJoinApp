package academy.learnprogramming.subjoin;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import de.hdodenhof.circleimageview.CircleImageView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FriendsActivity extends AppCompatActivity
{
    private RecyclerView myFriendList;
    private DatabaseReference FriendsRef, UsersRef;
    private FirebaseAuth mAuth;
    private String online_user_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        mAuth = FirebaseAuth.getInstance();
        online_user_id = mAuth.getCurrentUser().getUid();
        FriendsRef = FirebaseDatabase.getInstance().getReference().child("Friends").child(online_user_id);
        UsersRef = FirebaseDatabase.getInstance().getReference().child("Users");

        myFriendList =(RecyclerView) findViewById(R.id.friend_list);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        myFriendList.setLayoutManager(linearLayoutManager);

        DisplayAllFriends();
    }


    public void updateUserStatus(String state)
    {
        String saveCurrentDate, saveCurrentTime;

        Calendar calForDate = Calendar.getInstance();
        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyyy");
        saveCurrentDate = currentDate.format(calForDate.getTime());

        Calendar calForTime = Calendar.getInstance();
        SimpleDateFormat currentTime = new SimpleDateFormat("hh:mm a");
        saveCurrentTime = currentTime.format(calForTime.getTime());

        Map currentStateMap = new HashMap();
        currentStateMap.put("time", saveCurrentTime);
        currentStateMap.put("date", saveCurrentDate);
        currentStateMap.put("type", state);

        UsersRef.child(online_user_id).child("userState")
                .updateChildren(currentStateMap);
    }


    @Override
    protected void onStart()
    {
        super.onStart();

        updateUserStatus("online");
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        updateUserStatus("offline");
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();

        updateUserStatus("offline");
    }


    private void DisplayAllFriends()
    {
        FirebaseRecyclerOptions<Friends> options = new FirebaseRecyclerOptions.Builder<Friends>().setQuery(FriendsRef,Friends.class).build();
        FirebaseRecyclerAdapter<Friends, FriendsViewHolder> firebaseRecyclerAdapter
                = new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(options) {
            @Override
            protected void onBindViewHolder(@NonNull final FriendsViewHolder friendsViewHolder, int position, @NonNull Friends model)
            {
                friendsViewHolder.date.setText("Friends Since: "+model.getDate());
                final String usersIDs = getRef(position).getKey();

                assert usersIDs != null;
                UsersRef.child(usersIDs).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot)
                    {
                        if (dataSnapshot.exists())
                        {
                            final String userName = dataSnapshot.child("fullname").getValue(String.class);
                            final String profileImage = dataSnapshot.child("profileimage").getValue(String.class);
                            friendsViewHolder.allusernames.setText(userName);
                            Picasso.get().load(profileImage).placeholder(R.drawable.profile).into(friendsViewHolder.alluserprofilepicture);

                            friendsViewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v)
                                {
                                  CharSequence options[] = new  CharSequence[]
                                          {
                                            userName + "'s Profile",
                                            "Send Message"
                                          };
                                    AlertDialog.Builder builder = new AlertDialog.Builder(FriendsActivity.this);
                                    builder.setTitle("Select Option");

                                    builder.setItems(options, new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialogInterface, int which)
                                        {
                                          if (which == 0)
                                          {
                                              Intent profileintent = new Intent(FriendsActivity.this, PersonProfileActivity.class);
                                              profileintent.putExtra("visit_user_id",usersIDs);
                                              startActivity(profileintent);
                                          }
                                          if (which == 1)
                                          {
                                              Intent Chatintent = new Intent(FriendsActivity.this, ChatActivity.class);
                                              Chatintent.putExtra("visit_user_id",usersIDs);
                                              Chatintent.putExtra("userName",userName);
                                              startActivity(Chatintent);
                                          }
                                        }
                                    });
                                    builder.show();
                                }
                            });

                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

            }

            @NonNull
            @Override
            public FriendsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
            {
                View view= LayoutInflater.from(parent.getContext()).inflate(R.layout.all_users_display_layout,parent,false);
                FriendsViewHolder viewHolder=new FriendsViewHolder(view);
                return viewHolder;
            }
        };
        myFriendList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.startListening();
    }


    public static class FriendsViewHolder extends RecyclerView.ViewHolder
    {
        final CircleImageView alluserprofilepicture;
        final TextView allusernames, date;

        public FriendsViewHolder(@NonNull View itemView)
        {
            super(itemView);

            alluserprofilepicture = itemView.findViewById(R.id.all_users_profile_image);
            allusernames = itemView.findViewById(R.id.all_users_profile_full_name);
            date = itemView.findViewById(R.id.all_users_status);

        }

    }
}
