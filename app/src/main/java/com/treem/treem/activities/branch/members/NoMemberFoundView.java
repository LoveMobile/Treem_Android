package com.treem.treem.activities.branch.members;

import android.content.Context;
import android.os.Handler;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;
import com.treem.treem.R;
import com.treem.treem.activities.users.UserProfileActivity;
import com.treem.treem.application.CurrentTreeSettings;
import com.treem.treem.helpers.NotificationHelper;
import com.treem.treem.helpers.security.Phone.PhoneUtil;
import com.treem.treem.models.branch.Branch;
import com.treem.treem.models.user.User;
import com.treem.treem.models.user.UserAdd;
import com.treem.treem.services.Treem.TreemSeedingService;
import com.treem.treem.services.Treem.TreemService;
import com.treem.treem.services.Treem.TreemServiceRequest;
import com.treem.treem.services.Treem.TreemServiceResponseCode;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * No members found view handler
 */
public class NoMemberFoundView extends FrameLayout {
    //Edit control for edit phone number
    private EditText editInviteNumber;

    //Invite button
    private Button buttonInvite;

    //Invite progress
    private ProgressBar progressInvite;

    //Formatted phone number
    private String formattedPhone;

    //Selected branch
    private Branch branch;

    //LAst netwok search task
    private TreemService.NetworkRequestTask task;

    //Selected user
    private User member;

    //User view
    private ViewGroup memberView;

    //Member views
    private ImageView avatar;
    private TextView name;
    private TextView contactNameView;
    private ImageView status;
    private ImageView hexagon;
    private TextView username;
    private TextView phone;
    private LinearLayout colorsView;
    private int colorBranchSize;

    private Handler handler = new Handler();
    private ScrollView scroll;

    public NoMemberFoundView(Context context) {
        super(context);
    }

    public NoMemberFoundView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public NoMemberFoundView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        editInviteNumber = (EditText)findViewById(R.id.editPhone);
        editInviteNumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                hideMember();
                boolean enabled = isValidPhoneNumber(s.toString());

                // check if continue button should be enabled
                if (enabled){
                    performSearchUserByPhoneNumber(formattedPhone);
                }
                else
                    updateButton(false,true);

            }
        });
        buttonInvite = (Button)findViewById(R.id.inviteButton);
        buttonInvite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (member!=null)
                    invite(member.getId(),formattedPhone);
                else
                    invite(null,formattedPhone);
            }


        });
        progressInvite = (ProgressBar)findViewById(R.id.progressInvite);
        memberView = (ViewGroup)findViewById(R.id.member);
        avatar = (ImageView)findViewById(R.id.userAvatar);
        ImageView checkMark = (ImageView) findViewById(R.id.checkMark);
        name = (TextView)findViewById(R.id.memberName);
        contactNameView = (TextView)findViewById(R.id.contactName);
        status = (ImageView)findViewById(R.id.memberIcon);
        hexagon = (ImageView)findViewById(R.id.hexagon);
        username = (TextView)findViewById(R.id.memberUsername);
        phone = (TextView)findViewById(R.id.memberPhone);
        colorsView = (LinearLayout)findViewById(R.id.memberColors);
        checkMark.setVisibility(View.GONE);
        colorBranchSize = getResources().getDimensionPixelSize(R.dimen.member_branch_color_size);
        scroll = (ScrollView)findViewById(R.id.scroll);
        updateButton(false,true);

    }

    /**
     * Hide member view widget
     */
    private void hideMember() {
        member = null;
        memberView.setVisibility(View.GONE);
    }

    /**
     * Show no members view
     */
    public void show(){
        setVisibility(View.VISIBLE);
    }

    /**
     * Hide no members view
     */
    public void hide(){
        setVisibility(View.GONE);
        if (task!=null)
            task.cancel(true);
        member = null;
        editInviteNumber.setText("");
    }

    /**
     * Do a phone search by user phone
     * @param formattedPhone user phone
     */
    private void performSearchUserByPhoneNumber(String formattedPhone) {
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                task = null;
                updateProgress(false);
                if (!TextUtils.isEmpty(data)&&!"\"\"".equals(data)) {
                    try {
                        List<User> users = new Gson().fromJson(data,User.LIST_TYPE);
                        if (users.size()>0){
                            showMember(users.get(0));
                            updateButton(true,false);
                            return;
                        }

                    } catch (JsonSyntaxException e) {
                        e.printStackTrace();
                    }
                }
                updateButton(true, true);
            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                task = null;
                updateProgress(false);
                if (error==TreemServiceResponseCode.CANCELED)
                    return;
                buttonInvite.setVisibility(VISIBLE);
                updateButton(true,true);
            }
        };
        MembersSearchConfig config = new MembersSearchConfig();
        config.getMatching().setEmail(false);
        config.getMatching().setFirstName(false);
        config.getMatching().setLastName(false);
        config.getMatching().setUserName(false);
        config.getMiscellaneous().setAllowUseContacts(false);
        updateProgress(true);
        task = TreemSeedingService.searchUsers(request,branch==null?0:branch.id,formattedPhone,config,null,null,1,25,CurrentTreeSettings.SHARED_INSTANCE.treeSession);
    }

    /**
     * Show member views
     * @param user found member
     */
    private void showMember(User user) {
        member = user;
        if (member==null){
            hideMember();
            return;
        }
        memberView.setVisibility(View.VISIBLE);

        Context context = getContext();
        if (context!=null){
            //show avatar
            Picasso.with(context)
                    .load(user.getAvatarStreamUrl())
                    .placeholder(R.drawable.img_avatar)
                    .error(R.drawable.img_avatar)
                    .into(avatar);
        }

        avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (member!=null)
                    UserProfileActivity.showUserProfile(getContext(),member.getId());
            }
        });

        name.setText(user.getName());
        switch (user.getStatus()){
            case User.STATUS_FRIENDS:
                status.setImageResource(R.drawable.img_friends);
                break;
            case User.STATUS_INVITED:
                status.setImageResource(R.drawable.img_pending);
                break;
            case User.STATUS_PENDING:
                status.setImageResource(R.drawable.img_pending);
                break;
            default:status.setImageBitmap(null);

        }
        if (user.getContact()!=null){
            String contactName = user.getContact().getName();
            if (contactName!=null){
                contactName = " - "+contactName;
                contactNameView.setText(contactName);
            }
        }
        else
            contactNameView.setText("");
        hexagon.setVisibility(View.VISIBLE);
        username.setText(user.getUsername());
        colorsView.setVisibility(View.VISIBLE);
        colorsView.removeAllViews();
        List<String> colors = user.getColorsList();
        if (colors!=null){

            for (int i=0;i<colors.size();i++){
                View v = new View(context);
                int color = user.getColor(i);
                v.setBackgroundColor(color);
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(colorBranchSize, colorBranchSize);
                params.setMargins(colorBranchSize,0, 0,0);
                v.setLayoutParams(params);
                colorsView.addView(v,params);
            }
        }
        phone.setVisibility(View.GONE);
        phone.setVisibility(View.VISIBLE);
        phone.setText(formattedPhone);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                scroll.fullScroll(View.FOCUS_DOWN);
            }
        },100);

    }

    /**
     * Update invite button
     * @param isEnabled is button enabled
     * @param isInvite is search user may be invited
     */
    private void updateButton(boolean isEnabled,boolean isInvite) {
        if (isInvite)
            buttonInvite.setText(R.string.invite);
        else
            buttonInvite.setText(R.string.add_user);
        buttonInvite.setEnabled(isEnabled);
    }

    /**
     * Update progress view
     * @param isProgressVisible is progress visible?
     */
    private void updateProgress(boolean isProgressVisible){
        buttonInvite.setVisibility(isProgressVisible?View.GONE:View.VISIBLE);
        progressInvite.setVisibility(isProgressVisible?View.VISIBLE:View.GONE);
    }

    /**
     * Send invite/add member request
     * @param id user id (for add member)
     * @param formattedPhone phone number (for not treem users)
     */
    private void invite(final Long id, String formattedPhone) {
        UserAdd user = new UserAdd(id,formattedPhone);
        TreemServiceRequest request = new TreemServiceRequest() {
            @Override
            public void onSuccess(String data) {
                editInviteNumber.setText("");
                progressInvite.setVisibility(View.GONE);
                buttonInvite.setVisibility(View.VISIBLE);

            }

            @Override
            public void onFailure(TreemServiceResponseCode error, boolean wasHandled) {
                if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_1){
                    NotificationHelper.showError(getContext(),R.string.error_seeding_invalid_parameters_passed);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_2){
                    NotificationHelper.showError(getContext(),R.string.error_seeding_invalid_branch_id);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_3){
                    NotificationHelper.showError(getContext(),R.string.error_seeding_user_dont_exists);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_4){
                    NotificationHelper.showError(getContext(),R.string.error_seeding_invitation_code_generation_failed);
                }
                else if (error==TreemServiceResponseCode.GENERIC_RESPONSE_CODE_5){
                    NotificationHelper.showError(getContext(),R.string.error_try_seed_self);
                }
                else
                    NotificationHelper.showError(getContext(),getContext().getString(id==null?R.string.seeding_failed_invite_user:R.string.seeding_failed_add_user,error.getDescription(getContext())));

                updateProgress(false);
            }
        };
        progressInvite.setVisibility(View.VISIBLE);
        buttonInvite.setVisibility(View.GONE);
        Set<UserAdd> users = new HashSet<>();
        users.add(user);
        TreemSeedingService.setUsers(CurrentTreeSettings.SHARED_INSTANCE.treeSession,
                users, branch==null?0:branch.id, request);

    }

    /**
     * Check is entered phone valid
     * @param phone the phone number
     * @return true if valid
     */
    private boolean isValidPhoneNumber(String phone) {
        if (phone == null) {
            return false;
        }
        else if (phone.trim().length() < 1) {
            return false;
        }

        phone = PhoneUtil.getE164FormattedString(phone);

        this.formattedPhone = phone;

        return (phone != null);
    }

    /**
     * Set current branch
     * @param branch current branch
     */
    public void setBranch(Branch branch) {
        this.branch = branch;
    }
}
