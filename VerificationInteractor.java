public class VerificationInteractor implements IVerificationInteractor {

    private static final String TIMESTAMP_USER_ENTER = "timestamp_user_time";

    private IVerificationRepository repository;
    private ISaveRepository saveRepository;
    private IWrapperLinkKeyValue wrapperLinkKeyValue;

    private static final int POLLING_TIMEOUT = 45000;

    private Predicate<OobFetchMessageResponse> filterPredicate = oobFetchMessageResponse -> {
        if(oobFetchMessageResponse.isSucceeded() && oobFetchMessageResponse.getOobIncomingMessage() != null) {
            if(oobFetchMessageResponse.getOobIncomingMessage().isAcknowledgmentRequested()) {
                if(oobFetchMessageResponse.getOobIncomingMessage().getMessageType().equalsIgnoreCase(OobIncomingMessageType.USER_MESSAGE)) {
                    return true;
                }
            }
        }
        return false;
    };

    @Inject
    public VerificationInteractor(IVerificationRepository repository, ISaveRepository saveRepository, IWrapperLinkKeyValue linkKeyValue) {
        this.repository = repository;
        this.saveRepository = saveRepository;
        wrapperLinkKeyValue = linkKeyValue;
    }

    private Observable<OobFetchMessageResponse> fetchMessageFromOOB() {
        String finalClientId = clientId;
        String finalProviderId = providerId;

        return Observable.defer(() -> {
            return Observable.just(sdk.fetchMessage(finalClientId, finalProviderId));
        });
    }

    private Observable<OobResponse> getOobResponse(OobIncomingMessage message) {
        return Observable.defer(() ->
                Observable.just(sdk.acknowledgeMessage(message))
        );
    }

    @Override
    public Completable sendCancel(String content) {
        return Completable.fromSingle(Single.defer(() ->
                Single.just(sdk.sendMessageOutComing(getOobProviderToUserMessage(), getIdFirstMessage(), content))
        ));
    }

    @Override
    public Observable<Data> fetchMessage() {
        return fetchMessageFromOOB()
                        .repeatWhen(completed -> completed.delay(BuildConfig.TIME_FETCH, TimeUnit.SECONDS))
                        .doOnNext(response -> checkTimeout(response))
                        .subscribeOn(Schedulers.io())
                        .takeUntil(filterPredicate)
                        .filter(filterPredicate)
                        .doOnNext(oobFetchMessageResponse -> getOobResponse(oobFetchMessageResponse.getOobIncomingMessage()))
                        .map(oobFetchMessageResponse -> oobFetchMessageResponse.getOobIncomingMessage())
                        .map(message -> {
                            DataFactory dataFactory = new DataFactory();
                            return dataFactory.getData(oobPayment);
                        })
                        .retry()
                        .observeOn(AndroidSchedulers.mainThread());
    }

}
