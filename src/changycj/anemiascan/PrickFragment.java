package changycj.anemiascan;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;

public class PrickFragment extends Fragment implements OnClickListener{
	private Button prickNextButton;
	private final static int CAMERA_ACTIVITY_REQUEST_CODE = 1;
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_prick, container,
				false);
		
		prickNextButton = (Button) rootView.findViewById(R.id.prick_next);
		prickNextButton.setOnClickListener(this);
		
		((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(rootView.getWindowToken(),0);
		
		return rootView;
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CAMERA_ACTIVITY_REQUEST_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				Bundle args = getArguments();
				String patientName = args.getString("patientName");
				String patientId = args.getString("patientId");
				
				double count = data.getDoubleExtra("hemoCount", 0.0);
				String[] pixels = data.getStringArrayExtra("hemoPixels");
				String measure = data.getStringExtra("hemoMeasure");
				
				Bundle bundle = new Bundle();
				bundle.putDouble("hemoCount", count);
				bundle.putString("patientName", patientName);
				bundle.putString("patientId", patientId);
				bundle.putStringArray("hemoPixels", pixels);				
				bundle.putString("hemoMeasure", measure);
				ResultFragment result = new ResultFragment();
				result.setArguments(bundle);
				
				FragmentTransaction trans = getFragmentManager().beginTransaction();
				trans.replace(R.id.container, result);
				trans.addToBackStack(null);
				trans.commit();
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		Intent intent = new Intent(this.getActivity(), CameraActivity.class);
		startActivityForResult(intent, CAMERA_ACTIVITY_REQUEST_CODE);
	}
}
