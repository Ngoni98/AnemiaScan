package changycj.anemiascan;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

public class SignInFragment extends Fragment implements OnClickListener {

	public Button startScanButton;
	public Button instructionsButton;

	public SignInFragment() { }

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_signin, container,
				false);
		
		startScanButton = (Button) rootView.findViewById(R.id.sign_in_start_scan);
		startScanButton.setOnClickListener(this);
		
		instructionsButton = (Button) rootView.findViewById(R.id.sign_in_instructions);
		instructionsButton.setOnClickListener(this);
		
		FragmentManager manager = getFragmentManager();
		manager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

		
		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		EditText name = (EditText) getView().findViewById(R.id.sign_in_patient_name);
		name.setText("");
		EditText id = (EditText) getView().findViewById(R.id.sign_in_patient_id);
		id.setText("");
	}
	
	@Override
	public void onClick(View v) {
		FragmentTransaction trans = getFragmentManager().beginTransaction();

		switch(v.getId()) {
			case R.id.sign_in_start_scan:
				String name = ((EditText) getView().findViewById(R.id.sign_in_patient_name)).getText().toString();
				String id = ((EditText) getView().findViewById(R.id.sign_in_patient_id)).getText().toString();
				
				if (!name.isEmpty() && !id.isEmpty()) {
					PrickFragment prick = new PrickFragment();
					trans.replace(R.id.container, prick);
					
					Bundle bundle = new Bundle();
					bundle.putString("patientName", name);
					bundle.putString("patientId", id);
					prick.setArguments(bundle);
				}
				
				break;
			case R.id.sign_in_instructions:
				InstructionsFragment instructions = new InstructionsFragment();
				trans.replace(R.id.container, instructions);
				break;
		}
		trans.addToBackStack(null);
		trans.commit();

		
	}
	
}