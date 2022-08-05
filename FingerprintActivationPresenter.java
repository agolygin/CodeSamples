@InjectViewState
public class FingerprintActivationPresenter extends MvpPresenter<FingerprintActivationView> {

    @Inject
    IFingerprintInteractor fingerprintInteractor;

    public FingerprintActivationPresenter() {
        App.getComponent().inject(this);
    }

    public void activateFingerprintMode() {
        fingerprintInteractor.activateFingerprintMode()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if(false) {
                            getViewState().showFingerprintVerificationDialog();
                        } else {
                            getViewState().showFingerprintActivationCompleted();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        getViewState().showFingerprintActivationCompleted();
                    }
                });
    }

    public void deactivateFingerprintMode() {
        fingerprintInteractor.deactivateFingerprintMode();
        getViewState().showFingerprintActivationCompleted();
    }
}
