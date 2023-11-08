package lotto.domain;

import static camp.nextstep.edu.missionutils.Randoms.pickUniqueNumbersInRange;
import static lotto.domain.constants.LottoNumber.MAX_NUMBER;
import static lotto.domain.constants.LottoNumber.MIN_NUMBER;
import static lotto.domain.constants.LottoNumber.NUMBER_COUNT;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LottoService {
    private final static int INITIAL_VALUE = 0;
    private final static int COUNT_FOR_FIRST_RANKING = 6;
    private final static int COUNT_FOR_SECOND_RANKING = 1;
    private final static int COUNT_FOR_THIRD_RANKING = 5;
    private final static int COUNT_FOR_FOURTH_RANKING = 4;
    private final static int COUNT_FOR_FIFTH_RANKING = 3;
    private final static int FIRST_RANKING = 1;
    private final static int SECOND_RANKING = 2;
    private final static int THIRD_RANKING = 3;
    private final static int FOURTH_RANKING = 4;
    private final static int FIFTH_RANKING = 5;
    private final static int FIRST_RANKING_PRIZE = 2000000000;
    private final static int SECOND_RANKING_PRIZE = 30000000;
    private final static int THIRD_RANKING_PRIZE = 1500000;
    private final static int FOURTH_RANKING_PRIZE = 50000;
    private final static int FIFTH_RANKING_PRIZE = 5000;
    private final static int MAKE_PERCENT = 100;

    private final LottoRepository lottoRepository;
    private PurchaseCount purchaseCount;
    private WinningLotto winningLotto;
    private WinningResult winningResult;

    public LottoService(LottoRepository lottoRepository){
        this.lottoRepository = lottoRepository;
    }

    public Lottos findAllLottos(){
        return lottoRepository.findAll();
    }

    public void savePurchaseCount(int purchaseAmount) throws IllegalArgumentException {
        this.purchaseCount = new PurchaseCount(purchaseAmount);
    }

    public int findPurchaseCount() {
        return purchaseCount.getValue();
    }

    public void saveWinningLotto(List<Integer> numbers, int bonusNumber) {
        this.winningLotto = new WinningLotto(numbers, bonusNumber);
    }

    public WinningLotto findWinningLotto() {
        return winningLotto;
    }

    public void saveWinningResult(List<Integer> totalRankingCount) {
        this.winningResult = new WinningResult(totalRankingCount);
    }

    public WinningResult findWinningResult() {
        return winningResult;
    }

    public void issueLottos(int number) {
        List<Lotto> tempLottos = new ArrayList<>(number);

        for (int i = 0; i < number; i++) {
            tempLottos.add(new Lotto(createRandomNumberList()));
        }

        lottoRepository.saveAll(new Lottos(tempLottos));
    }

    private List<Integer> createRandomNumberList(){
        return pickUniqueNumbersInRange(MIN_NUMBER.getValue(), MAX_NUMBER.getValue(), NUMBER_COUNT.getValue());
    }

    public void createWinningResult(Lottos lottos, WinningLotto winningLotto){
        List<Integer> totalRankingCount = new ArrayList<>(Arrays.asList(0, 0, 0, 0, 0));

        for (Lotto lotto : lottos.getLottos()) {
            int matchingCount = INITIAL_VALUE;
            int matchingBonusCount = INITIAL_VALUE;
            for (int lottoNumber : lotto.getNumbers()) {
                matchingCount = getMatchingCount(winningLotto, lottoNumber, matchingCount);
                matchingBonusCount = getMatchingBonusCount(winningLotto,  lottoNumber, matchingBonusCount);
            }
            int ranking = getRanking(matchingCount, matchingBonusCount);
            putRanking(ranking, totalRankingCount);
        }

        saveWinningResult(totalRankingCount);
    }

    private static Integer getMatchingCount(WinningLotto winningLotto, int lottoNumber, int matchingCount) {
        if (winningLotto.getNumbers().contains(lottoNumber)) {
            matchingCount++;
        }
        return matchingCount;
    }

    private static Integer getMatchingBonusCount(WinningLotto winningLotto, int lottoNumber, int matchingBonusCount) {
        if (winningLotto.getBonusNumber() == lottoNumber) {
            matchingBonusCount++;
        }
        return matchingBonusCount;
    }

    private Integer getRanking(int matchingCount, int matchingBonusCount){
        if (matchingCount == COUNT_FOR_FIFTH_RANKING){ return FIFTH_RANKING; }
        if (matchingCount == COUNT_FOR_FOURTH_RANKING){ return FOURTH_RANKING; }
        if (matchingCount == COUNT_FOR_THIRD_RANKING){
            if (matchingBonusCount == COUNT_FOR_SECOND_RANKING){ return SECOND_RANKING; }
            return THIRD_RANKING;
        }
        if (matchingCount == COUNT_FOR_FIRST_RANKING){ return FIRST_RANKING; }
        return INITIAL_VALUE;
    }

    private static void putRanking(int ranking, List<Integer> totalRankingCount) {
        if (ranking!=INITIAL_VALUE) {
            int index = ranking - 1;
            int count = totalRankingCount.get(index);
            totalRankingCount.set(index, count + 1);
        }
    }

    public Float getReturnRate(WinningResult winningResult, int purchaseAmount) {
        int totalReturn = calculateTotalReturn(winningResult);
        return (float) totalReturn / purchaseAmount * MAKE_PERCENT;
    }

    private Integer calculateTotalReturn(WinningResult winningResult) {
        return winningResult.getFirstPlaceCount() * FIRST_RANKING_PRIZE +
                winningResult.getSecondPlaceCount() * SECOND_RANKING_PRIZE +
                winningResult.getThirdPlaceCount() * THIRD_RANKING_PRIZE +
                winningResult.getFourthPlaceCount() * FOURTH_RANKING_PRIZE +
                winningResult.getFifthPlaceCount() * FIFTH_RANKING_PRIZE;
    }

}
