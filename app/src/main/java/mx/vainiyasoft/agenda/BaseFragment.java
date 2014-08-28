package mx.vainiyasoft.agenda;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.app.Fragment;
import android.app.FragmentTransaction;

/**
 * Created by alejandro on 8/27/14.
 */
public class BaseFragment extends Fragment {

    @Override
    public Animator onCreateAnimator(int transit, boolean enter, int nextAnim) {
        switch (transit) {
            case FragmentTransaction.TRANSIT_FRAGMENT_FADE:
                if (enter) return loadAnimator(android.R.animator.fade_in);
                else return loadAnimator(android.R.animator.fade_out);
            case FragmentTransaction.TRANSIT_FRAGMENT_CLOSE:
                if (enter) return loadAnimator(R.animator.fragment_pop_enter);
                else return loadAnimator(R.animator.fragment_pop_exit);
            case FragmentTransaction.TRANSIT_FRAGMENT_OPEN:
            default:
                if (enter) return loadAnimator(R.animator.fragment_enter);
                else return loadAnimator(R.animator.fragment_exit);
        }
    }

    private Animator loadAnimator(int resource) {
        return AnimatorInflater.loadAnimator(getActivity(), resource);
    }

}
