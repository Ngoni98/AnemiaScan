package changycj.anemiascan;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

public class InstructionsFragment extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_instructions, container,
				false);
		((InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE))
			.hideSoftInputFromWindow(rootView.getWindowToken(),0);
		return rootView;
	}
}
