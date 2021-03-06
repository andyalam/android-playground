package com.example.andy.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import static android.support.v4.content.ContextCompat.checkSelfPermission;


public class CrimeFragment extends Fragment {
    private static final String ARG_CRIM_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_TIME = "DialogTime";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_TIME = 1;
    private static final int REQUEST_CONTACT = 2;
    // Request code for READ_CONTACTS. It can be any number > 0.
    private static final int PERMISSIONS_REQUEST_READ_CONTACTS = 100;


    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private Button mTimeButton;
    private CheckBox mSolvedCheckBox;
    private Button mDeleteButton;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mCallSuspectButton;


    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIM_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIM_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
    }

    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mTimeButton = (Button) v.findViewById(R.id.crime_time);
        updateTime();
        mTimeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                TimePickerFragment dialog = TimePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_TIME);
                dialog.show(manager, DIALOG_TIME);
            }
        });

        mSolvedCheckBox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mDeleteButton = (Button) v.findViewById(R.id.crime_delete);
        mDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CrimeLab.get(getActivity()).deleteCrime(mCrime.getId());
                getActivity().finish();
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = ShareCompat.IntentBuilder.from(getActivity())
                        .setText(getCrimeReport())
                        .setSubject(getString(R.string.crime_report_subject))
                        .setType("text/plain")
                        .getIntent();
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });

        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        mCallSuspectButton = (ImageButton) v.findViewById(R.id.crime_call);
        mCallSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // check permissions
                requestContactPermission();


            }
        });

        // Make sure the phone supports contacts, if not, disable the suspect buttons
        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }

        return v;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_TIME) {
            // set time
            Date date = (Date) data.getSerializableExtra(TimePickerFragment.EXTRA_TIME);
            mCrime.setDate(date);
            updateTime();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();

            // specify which fields we want the query to return values for
            String[] queryFields = new String[] {
              ContactsContract.Contacts.DISPLAY_NAME
            };

            // perform query, contactUri = "where" clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                if (c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        }

    }

    private void updateDate() {
        SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy");
        String formatted_date = format.format(Date.parse(mCrime.getDate().toString()));
        mDateButton.setText(formatted_date);
    }

    private void updateTime() {
        SimpleDateFormat format = new SimpleDateFormat("hh:mm a");
        String formatted_time = format.format(Date.parse(mCrime.getDate().toString()));
        mTimeButton.setText(formatted_time);
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, mCrime.getSuspect());
        }

        String report = getString(R.string.crime_report, mCrime.getTitle(),
                dateString, solvedString, suspect);

        return report;
    }

    /**
     * Show the contacts in the ListView.
     */
    private void requestContactPermission() {
        // Check the SDK version and whether the permission is already granted or not.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(getContext(), Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, PERMISSIONS_REQUEST_READ_CONTACTS);
            //After this point you wait for callback in onRequestPermissionsResult(int, String[], int[]) overriden method
        } else {
            // Android version is lesser than 6.0 or the permission is already granted.
            // Proceed with rest of code
            queryContactsDisplayResult();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission is granted
                requestContactPermission();
            } else {
                Toast.makeText(getContext(), "Until you grant the permission, we canot display the names", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void queryContactsDisplayResult() {
        String contactId;
        String phoneNumber;

        // query contacts db for contact
        Uri contactUri = ContactsContract.Data.CONTENT_URI;

        // specify which fields we want the query to return values for
        String[] queryFields = new String[] {
                ContactsContract.Contacts._ID
        };

        String selection = ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME + " = ?";
        String[] selectionArguments = { mSuspectButton.getText().toString() };

        // perform query, contactUri = "where" clause here
        Cursor c = getActivity().getContentResolver()
                .query(contactUri, queryFields, selection, selectionArguments, null);
        try {
            if (c.getCount() == 0) {
                return;
            }

            c.moveToFirst();
            contactId = c.getString(0);
        } finally {
            c.close();
        }


        // use contactId to query for contact's phone number now
        // specify which fields we want the query to return values for
        Uri phoneUri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        String[] queryPhoneFields = new String[] {
                ContactsContract.CommonDataKinds.Phone.NUMBER
        };

        String phoneSelection = ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?";
        String[] selectionPhoneArguments = { contactId };

        // perform query, contactUri = "where" clause here
        Cursor phoneC = getActivity().getContentResolver()
                .query(phoneUri, queryPhoneFields, phoneSelection, selectionPhoneArguments, null);

        try {
            if (phoneC.getCount() == 0) {
                return;
            }

            phoneC.moveToFirst();
            phoneNumber = phoneC.getString(0);
        } finally {
            phoneC.close();
        }

        if (phoneNumber != "") {
            mSuspectButton.setText(phoneNumber);
        } else {
            mSuspectButton.setText("empty");
        }
        Log.d("123", "hit this point3");
        // launch implicit intent to dial number
    }
}
