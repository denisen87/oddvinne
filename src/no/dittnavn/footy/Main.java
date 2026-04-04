package no.dittnavn.footy;

import no.dittnavn.footy.analysis.ProbabilityAnalysis;
import no.dittnavn.footy.analysis.OddsAnalyzer;
import no.dittnavn.footy.analysis.SimulationResult;
import java.util.Scanner;
import service.MatchService;
import no.dittnavn.footy.model.Match;
import no.dittnavn.footy.model.Outcome;
import no.dittnavn.footy.stats.StatsIndeks;
import no.dittnavn.footy.stats.TeamStats;
import no.dittnavn.footy.model.TeamProfile;
import no.dittnavn.footy.scanner.ValueScanner;
import no.dittnavn.footy.scanner.MatchOdds;
import no.dittnavn.footy.analysis.learning.LearningEngine;
import no.dittnavn.footy.analysis.learning.PredictionRecord;
import java.util.Map;
import java.util.HashMap;
import no.dittnavn.footy.analysis.learning.ModelWeights;
import no.dittnavn.footy.analysis.NeuralModel;
import no.dittnavn.footy.analysis.features.MatchFeatures;
import no.dittnavn.footy.analysis.bet.BankrollManager;
import no.dittnavn.footy.analysis.bet.FullAutoSimulator;
import no.dittnavn.footy.analysis.simulation.ManualBettingSimulator;

import no.dittnavn.footy.model.MatchRecord;
import no.dittnavn.footy.analysis.ProbabilityCalibrator;
import no.dittnavn.footy.analysis.bet.KellyCalculator;
import no.dittnavn.footy.analysis.Team.TeamDNA;
import no.dittnavn.footy.analysis.learning.AutoDataTrainer;
import java.util.List;

import no.dittnavn.footy.util.TeamNameNormalizer;
import no.dittnavn.footy.integration.playars.FootballDataPlayerClient;

import no.dittnavn.footy.integration.playars.TeamIdMapper;
import no.dittnavn.footy.stats.StatsRepository;
import no.dittnavn.footy.analysis.EloPredictor;
import no.dittnavn.footy.analysis.odds.OddsConverter;
import no.dittnavn.footy.analysis.ensemble.EnsemblePredictor;

import java.util.ArrayList;

import no.dittnavn.footy.util.CsvMerger;
import java.io.File;

import no.dittnavn.footy.analysis.AutoUpdater;
import no.dittnavn.footy.analysis.poisson.PoissonPredictor;
import no.dittnavn.footy.db.DatabaseManager;
import no.dittnavn.footy.analysis.GlobalConfidence;
import no.dittnavn.footy.analysis.ModelTrainer;
import no.dittnavn.footy.analysis.NeuralTrainer;
import no.dittnavn.footy.engine.AutoPredictionEngine;
import no.dittnavn.footy.engine.AutoBetEngine;
import no.dittnavn.footy.analysis.learning.PredictionTracker;
import no.dittnavn.footy.loader.OddsCsvLoader;
import no.dittnavn.footy.loader.CsvHistoricalLoader;
import no.dittnavn.footy.loader.FootballDataLoader;
import no.dittnavn.footy.loader.CsvMatchLoader;
import no.dittnavn.footy.stats.GlobalStats;
import no.dittnavn.footy.engine.FullAutoOptimizer;
import no.dittnavn.footy.loader.CsvFixtureLoader;
import service.ResultUpdater;
import no.dittnavn.footy.config.StrategyConfig;
import no.dittnavn.footy.engine.BacktestRunner;
import java.sql.Connection;
import no.dittnavn.footy.engine.FeatureConfig;




public class Main {


    public static void main(String[] args) throws Exception {


        StatsRepository repo = new StatsRepository();
        DatabaseManager.init();

        StatsIndeks stats = new StatsIndeks();
        GlobalStats.stats = stats;
        StatsIndeks indeks = GlobalStats.stats;
        System.out.println("Teams loaded: " + GlobalStats.stats.getAllTeams().size());
        NeuralModel neural = new NeuralModel();
        PredictionTracker tracker = new PredictionTracker();
        EnsemblePredictor ensemble = new EnsemblePredictor();



        Scanner scanner = new Scanner(System.in);

        System.out.println("1. Live modus");
        System.out.println("2. Backtest historisk");
        int mode = scanner.nextInt();

        // =========================
        // BACKTEST MODUS
        // =========================
        if (mode == 2) {

            List<Match> historicalMatches = new ArrayList<>();

            String[] leagues = {
                    "E0", "E1", "D1", "D2", "I1", "I2", "SP1", "SP2", "F1", "F2", "B1", "P1", "T1"
            };

            for (String league : leagues) {

                String path = "data/" + league + ".csv";

                File file = new File(path);
                if (!file.exists()) {
                    System.out.println("Missing file: " + path);
                    continue;
                }

                List<Match> loaded = CsvHistoricalLoader.load(path, league);

                System.out.println("Loaded " + league + ": " + loaded.size());

                historicalMatches.addAll(loaded);
            }



// 🔍 DEBUG
            System.out.println("After odds filter: " + historicalMatches.size());

            System.out.println("TOTAL MATCHES: " + historicalMatches.size());

            // 🔥 FILTER BORT KAMPER UTEN ODDS
            historicalMatches = historicalMatches.stream()
                    .filter(m -> m.getHomeOdds() > 1.01 && m.getAwayOdds() > 1.01)
                    .toList();

// 🔍 DEBUG
            System.out.println("After odds filter: " + historicalMatches.size());

// 🔥 KUN TESTDATA
            historicalMatches = historicalMatches.subList(0, 300);

            FeatureConfig config = new FeatureConfig();
            config.useSOT = true;
            config.useConfidence = true;
            config.useHomeBias = true;

            // 🔥 BYGG STATS FRA CSV (IKKE DB)
            stats = new StatsIndeks();

            for (Match m : historicalMatches) {
                stats.update(m);
            }

            GlobalStats.stats = stats;

            System.out.println("Teams loaded from CSV: " + stats.getAllTeams().size());

            BacktestRunner.run(
                    historicalMatches,
                    0.60,
                    0.55,
                    0.05,
                    1.0,
                    0.02,
                    1.6,
                    2.4,
                    0.50,
                    config
            );
/*
            BacktestRunner.run(
                    historicalMatches,
                    0.60,
                    0.55,
                    0.10,   // 🔥 enda strengere
                    1.0,
                    0.03,
                    1.6,
                    2.4,
                    0.50
            );
/*
            ThresholdOptimizer.runOptimization(historicalMatches);

 */
/*
            BacktestRunner.run(
                    historicalMatches,
                    StrategyConfig.MAX_PROB,
                    StrategyConfig.PROB,
                    StrategyConfig.EDGE,
                    StrategyConfig.HOME_BIAS,
                    StrategyConfig.CONFIDENCE,
                    StrategyConfig.MIN_ODDS,
                    StrategyConfig.MAX_ODDS,
                    StrategyConfig.PROB_THRESHOLD
            );

 */

            return; // STOPPER HER


        }








        // =========================
        // LIVE MODUS FORTSETTER
        // =========================


        ModelWeights weights = new ModelWeights();


        System.out.println("=== TEAMS ===");
/*
        GlobalStats.stats.getAllTeams().stream()
                .limit(20)
                .forEach(t -> System.out.println(t.getName()));

 */


        System.out.println("LOOKUP argentinos jrs: " +
                GlobalStats.stats.getTeam("argentinos jrs"));
        /*

        GlobalStats.stats.getAllTeams().stream()
                .filter(t -> t.getName().contains("argent"))
                .forEach(t -> System.out.println("FOUND: " + t.getName()));

         */



        stats = GlobalStats.stats;

        MatchService matchService = new MatchService(GlobalStats.stats);

        Map<String, PredictionRecord> pending = new HashMap<>();
        Map<String, MatchFeatures> pendingFeatures = new HashMap<>();
        ValueScanner scannerAI = new ValueScanner(weights);
        ProbabilityAnalysis analysis = new ProbabilityAnalysis(weights);
        BankrollManager bankroll = new BankrollManager();
        FullAutoSimulator autosim = new FullAutoSimulator(neural, bankroll);
        ManualBettingSimulator manualsim =
                new ManualBettingSimulator(neural, analysis, bankroll);

        LearningEngine learning = new LearningEngine(weights, neural);
        ProbabilityCalibrator calibrator = new ProbabilityCalibrator();
        KellyCalculator kelly = new KellyCalculator();

        DatabaseManager db = new DatabaseManager();

        AutoDataTrainer autoTrainer =
                new AutoDataTrainer(indeks);

        AutoUpdater autoUpdater =
                new AutoUpdater(indeks, autoTrainer);

        boolean runOptimizer = false; // 👈 sett til false for at OptimizerMain skal kjøre,



        List<Match> matches;

        if (runOptimizer) {

            matches =
                    DatabaseManager.getHistoricalMatchesOrdered();

            System.out.println("Matches BEFORE filter: " + matches.size());

            matches = matches.stream()
                    .filter(m -> m.getHomeOdds() > 1.01 && m.getAwayOdds() > 1.01)
                    .toList();

            System.out.println("Matches AFTER filter: " + matches.size());

            FullAutoOptimizer.run(matches);



            return;

        } else {

            // 1️⃣ hent historikk + tren modeller
            autoUpdater.run();

            // 2️⃣ tren neural ferdig etter data er lastet
            learning.trainFromReality(neural);
        }





        // =========================
        // 1️⃣ MERGE EVENTUELLE NYE CSV-FILER
        // =========================

        if (new File("import/T1_new.csv").exists()) {
            CsvMerger.merge("data/T1_2025.csv", "import/T1_new.csv");
        }

        if (new File("import/B1_new.csv").exists()) {
            CsvMerger.merge("data/B1.csv", "import/B1_new.csv");
        }

        if (new File("import/SC0_new.csv").exists()) {
            CsvMerger.merge("data/SC0.csv", "import/SC0_new.csv");
        }


        // =========================
        // 2️⃣ LAST HISTORISKE CSV-KAMPER
        // =========================

        matches = new ArrayList<>();

        File dataFolder = new File("data");

        for (File file : dataFolder.listFiles()) {

            System.out.println("FILE: " + file.getName());

            if (file.getName().endsWith(".csv")
                    && !file.getName().contains("odds")) {

                String fileName = file.getName();

                String league = fileName
                        .replace("historical_", "")
                        .replace(".csv", "")
                        .split("_")[0];

                System.out.println("Laster: " + fileName + " | League: " + league);

                List<Match> loaded = CsvHistoricalLoader.load(file.getPath(), league);

                for (Match m : loaded) {
                    m.setLeague(league);
                }

                matches.addAll(loaded);
            }

        }


        System.out.println("Totale kamper lest fra CSV: " + matches.size());

        // =========================
        // 3️⃣ TRENE MODELL FRA CSV
        // =========================

        autoTrainer.trainFromMatches(matches);

        // =========================
        // 4️⃣ AUTO UPDATE FRA API (INCREMENTAL)
        // =========================

        System.out.println("\n=== AUTO UPDATE AV ALLE LIGAER ===");

        System.out.println("=== AUTO UPDATE FULLFØRT ===\n");

        // =========================
        // 5️⃣ LAGRE ALT
        // =========================

        repo.save(indeks);
        System.out.println("Stats lagret til stats.json");

        System.out.println("Antall lag i indeks: " + indeks.teamCount());


        // =========================
        // DEBUG – VIS LAG I MODELL
        // =========================

        System.out.println("=== LAG I STATSINDEKS ===");

        System.out.println("=== SLUTT ===\n");

// 🔥 1️⃣ Hent ferske Championship fixtures fra Flashscore
        /*
        FlashscoreFixtureFetcher.fetchE1Fixtures();
        */


// LAST fixtures FRA CSV
        System.out.println("Leser fra: " +
                new java.io.File("data/E1_fixtures.csv").getAbsolutePath());

        List<Match> e1 = CsvFixtureLoader.load("data/E1_fixtures.csv");
        System.out.println("E1 fixtures: " + e1.size());

        List<Match> b1 = CsvFixtureLoader.load("data/B1_fixtures.csv");
        System.out.println("B1 fixtures: " + b1.size());

        List<Match> sc0 = CsvFixtureLoader.load("data/SC0_fixtures.csv");
        System.out.println("SC0 fixtures: " + sc0.size());

// legg til etterpå
        List<Match> upcoming = new ArrayList<>();
        upcoming.addAll(e1);
        upcoming.addAll(b1);
        upcoming.addAll(sc0);


        List<String> leagues = List.of("europa-league");
/*
        List<Match> upcoming =
                OddsCsvLoader.load("data/odds_today.csv");

 */



        System.out.println("Totale upcoming fixtures: " + upcoming.size());

        System.out.println("Totale upcoming fixtures: " + upcoming.size());

        System.out.println("Totale upcoming fixtures: " + upcoming.size());
/*
        List<FlashscoreResultFetcher.MatchResult> elResults =
                FlashscoreResultFetcher.fetchEuropaLeague2025Results();
        for (FlashscoreResultFetcher.MatchResult r : elResults) {
            matchService.registerMatch(
                    r.homeTeam,
                    r.awayTeam,
                    r.homeGoals,
                    r.awayGoals
            );
        }

 */
        ResultUpdater.updateResults();

        AutoPredictionEngine.run(indeks, neural, tracker, upcoming);

        AutoBetEngine.run(indeks, neural, tracker, upcoming);
/*
        no.dittnavn.footy.engine.BacktestEngine.run(); // bruker backtestrunner nå istede,

 */

        int valg = -1;

        while (valg != 0) {
// skriver ut de ulike menyvalgene
            System.out.println("=== FOTBALLANALYSE ===");
            System.out.println("1. Registrer kamp");
            System.out.println("2. Vis lagstatistikk");
            System.out.println("3. Beregn sannsynlighet for kamp");
            System.out.println("4. Administrer laginformasjon");
            System.out.println("5. Legg kamp til value scanner");
            System.out.println("6. Scan etter value bets");
            System.out.println("7. AI simulering (betting test)");
            System.out.println("8. Manuell betting-simulering");
            System.out.println("9. Auto betting-simulering");
            System.out.println("10. ROI analyse");
            System.out.println("11. ROI per bet-type");
            System.out.println("12. Auto pre-match predictions");
            System.out.println("0. Avslutt");
            System.out.print("Velg: ");

            valg = readInt(scanner);
            scanner.nextLine();


            if (valg == 1) {

                System.out.print("Hjemmelag: ");
                String hjemmelag = scanner.nextLine();

                System.out.print("Bortelag: ");
                String bortelag = scanner.nextLine();

                System.out.print("Mål hjemmelag: ");
                int homeGoals = Integer.parseInt(scanner.nextLine());

                System.out.print("Mål bortelag: ");
                int awayGoals = Integer.parseInt(scanner.nextLine());

                hjemmelag = TeamNameNormalizer.normalize(hjemmelag);
                bortelag = TeamNameNormalizer.normalize(bortelag);

                // 🔥 bestem faktisk outcome
                Outcome outcome;
                if(homeGoals > awayGoals) outcome = Outcome.HOME;
                else if(homeGoals < awayGoals) outcome = Outcome.AWAY;
                else outcome = Outcome.DRAW;

                double actualHome = outcome == Outcome.HOME ? 1.0 : 0.0;
                double actualDraw = outcome == Outcome.DRAW ? 1.0 : 0.0;
                double actualAway = outcome == Outcome.AWAY ? 1.0 : 0.0;

                // 🔥 registrer kamp i stats
                matchService.registerMatch(hjemmelag, bortelag, homeGoals, awayGoals);

                // 🔥 hent lagstats fra indeks
                TeamStats homeStats = indeks.getTeam(hjemmelag);
                TeamStats awayStats = indeks.getTeam(bortelag);

                if(homeStats == null) homeStats = indeks.getOrCreate(hjemmelag);
                if(awayStats == null) awayStats = indeks.getOrCreate(bortelag);

                // 🔥 finn prediction som ble gjort før kamp
                PredictionRecord lastPrediction =
                        tracker.findLastPrediction(
                                hjemmelag,
                                bortelag
                        );

                MatchRecord last = null;

                if (lastPrediction != null) {
                    last = lastPrediction.toMatchRecord();
                }

                if(lastPrediction != null){

                    String predicted = lastPrediction.getBet();

                    String result;
                    if(homeGoals > awayGoals) result = "HOME";
                    else if(homeGoals < awayGoals) result = "AWAY";
                    else result = "DRAW";

                    boolean wasCorrect = predicted.equals(result);

                    double profit = 0;

// beregn profit basert på bet
                    if(predicted.equals(result)) {

                        if(result.equals("HOME"))
                            profit = lastPrediction.oddsHome * lastPrediction.stake - lastPrediction.stake;

                        else if(result.equals("DRAW"))
                            profit = lastPrediction.oddsDraw * lastPrediction.stake - lastPrediction.stake;

                        else if(result.equals("AWAY"))
                            profit = lastPrediction.oddsAway * lastPrediction.stake - lastPrediction.stake;

                    } else {
                        profit = -lastPrediction.stake;
                    }

// lagre profit i objektet
                    lastPrediction.profit = profit;

// 🔥 OPPDATER SQLITE
                    DatabaseManager.updateResult(
                            lastPrediction.dbId,
                            result,
                            profit
                    );

                    double confidence = lastPrediction.getConfidence();

                    // =========================
                    // 🧠 MODEL LEARNING
                    // =========================

                    // 1️⃣ ELO learning
                    ModelTrainer.adaptElo(homeStats, awayStats, confidence, result);

                    // 2️⃣ GLOBAL confidence learning
                    if(!wasCorrect && confidence > 0.75){
                        GlobalConfidence.adjustDown();
                    }
                    if(wasCorrect && confidence > 0.75){
                        GlobalConfidence.adjustUp();
                    }

                    // 3️⃣ Neural trust learning
                    NeuralTrainer.adjustTrust(wasCorrect, confidence);

                    // 4️⃣ lagre faktisk resultat i prediction
                    lastPrediction.actualOutcome = outcome;

                    // 5️⃣ AI lærer av realiteten
                    learning.learnFromReality(lastPrediction.toMatchRecord());

                    ensemble.updatePerformance(
                            lastPrediction.neuralProbs,
                            lastPrediction.eloProbs,
                            lastPrediction.poissonProbs,
                            lastPrediction.oddsProbs,
                            actualHome,
                            actualDraw,
                            actualAway
                    );
                }

                // oppdater tracker
                tracker.updateResult(
                        hjemmelag,
                        bortelag,
                        outcome
                );

                // tren neural etter kamp
                learning.trainFromReality(neural);

                // 🧠 pending feature learning
                String key = hjemmelag + "-" + bortelag;

                if(pendingFeatures.containsKey(key)){
                    MatchFeatures f = pendingFeatures.get(key);
                    neural.printWeights();
                    System.out.println("AI lærte fra kampen!");
                }

                System.out.println("Kamp registrert.\n");
            }


            else if (valg == 2) {

                System.out.print("Hvilket lag: ");
                scanner.nextLine(); // 🔥 VIKTIG

                String input = scanner.nextLine();

                input = input.replaceAll("[^a-zA-Z ]", "")
                        .toLowerCase()
                        .trim();

                TeamStats team = indeks.getTeam(input);

                if (team == null) {
                    System.out.println("Laget finnes ikke.");
                } else {
                    System.out.println(team);
                }
            }


            else if (valg == 3) {

                scanner.nextLine();

                System.out.print("Hjemmelag: ");
                String homeInput = scanner.nextLine();

                System.out.print("Bortelag: ");
                String awayInput = scanner.nextLine();

// 👉 rens + normaliser input først
                homeInput = TeamNameNormalizer.normalize(homeInput.trim().toLowerCase());
                awayInput = TeamNameNormalizer.normalize(awayInput.trim().toLowerCase());

                System.out.println("DEBUG home: " + homeInput);
                System.out.println("DEBUG away: " + awayInput);

// 👉 hent stats
                TeamStats homeStats = indeks.getTeam(homeInput);
                TeamStats awayStats = indeks.getTeam(awayInput);

// 👉 valider
                if (homeStats == null || awayStats == null) {
                    System.out.println("❌ Unknown team(s) – finnes ikke i datasettet");
                    break;
                }
                System.out.println("DEBUG home: " + homeInput);
                System.out.println("DEBUG away: " + awayInput);

                int homeId = TeamIdMapper.getId(homeInput);
                int awayId = TeamIdMapper.getId(awayInput);

                String homeJson = FootballDataPlayerClient.getTeamPlayers(homeId);
                String awayJson = FootballDataPlayerClient.getTeamPlayers(awayId);

                System.out.print("Odds hjemmeseier: ");
                double oddsHome = Double.parseDouble(scanner.nextLine());

                System.out.print("Odds uavgjort: ");
                double oddsDraw = Double.parseDouble(scanner.nextLine());

                System.out.print("Odds borteseier: ");
                double oddsAway = Double.parseDouble(scanner.nextLine());


                System.out.println("DEBUG home: " + homeInput);
                System.out.println("DEBUG away: " + awayInput);

/*
                System.out.println("=== ALLE LAG I INDEKS ===");
                indeks.printAllTeams();

 */





                if (homeStats == null) {
                    System.out.println("Oppretter nytt lag i indeks: " + homeInput);
                    homeStats = indeks.getOrCreate(homeInput);
                }

                if (awayStats == null) {
                    System.out.println("Oppretter nytt lag i indeks: " + awayInput);
                    awayStats = indeks.getOrCreate(awayInput);
                }

                EloPredictor elo = new EloPredictor();

                double[] eloProbs = elo.predict(homeStats, awayStats);

// 🔥 2) Neural prediction
                MatchFeatures features = new MatchFeatures(homeStats, awayStats, 1.0, 1.0, 1.0);
                double[] neuralProbs = neural.predictAll(features);


                System.out.println("DEBUG raw neural: "
                        + neuralProbs[0] + " "
                        + neuralProbs[1] + " "
                        + neuralProbs[2]);

// 👇 LEGG INN DENNE BLOKKEN HER
// 🔥 NEURAL STABILIZER

                for (int i = 0; i < 3; i++) {
                    if (neuralProbs[i] < 0.05) neuralProbs[i] = 0.05;
                    if (neuralProbs[i] > 0.90) neuralProbs[i] = 0.90;
                }

// re-normaliser
                double sumNeural = neuralProbs[0] + neuralProbs[1] + neuralProbs[2];

                for (int i = 0; i < 3; i++) {
                    neuralProbs[i] /= sumNeural;
                }

// 🔥 3) Odds → probability
                OddsConverter oddsConv = new OddsConverter();
                double[] oddsProbs = oddsConv.fromOdds(oddsHome, oddsDraw, oddsAway);

                PoissonPredictor poisson = new PoissonPredictor();
                double[] poissonProbs = poisson.predict(homeStats, awayStats);

                // 🔥 1) Elo prediction

                System.out.println("\n=== MODELLER ===");

                printProbs("Poisson", poissonProbs);
                printProbs("ELO", eloProbs);
                printProbs("Neural", neuralProbs);
                printProbs("Bookmaker", oddsProbs);

                // 🔥 4) Ensemble

                double[] finalProbs = ensemble.combine(neuralProbs, eloProbs, poissonProbs, oddsProbs);

                double finalHome = finalProbs[0];
                double finalDraw = finalProbs[1];
                double finalAway = finalProbs[2];

                double shrink = 0.85;

// trekk mot 33% (reduser ekstreme predictions)
                for (int i = 0; i < 3; i++) {
                    finalProbs[i] = finalProbs[i] * shrink + (1.0 / 3.0) * (1 - shrink);
                }


                System.out.println("\n=== ENSEMBLE (FINAL) ===");
                printProbs("Final", finalProbs);

                // --- DRAW CALIBRATION ---
                finalProbs[1] *= 0.92;   // reduser draw med 8%

// re-normaliser slik at summen = 1
                double sum = finalProbs[0] + finalProbs[1] + finalProbs[2];
                finalProbs[0] /= sum;
                finalProbs[1] /= sum;
                finalProbs[2] /= sum;

                double confidence = modelAgreementScore(
                        poissonProbs,
                        eloProbs,
                        neuralProbs,
                        oddsProbs
                );

                confidence -= GlobalConfidence.getPenalty();

                if (confidence < 0) confidence = 0;

                System.out.printf("MODEL CONFIDENCE: %.2f\n", confidence);



                System.out.println("DEBUG home: " + homeInput);
                System.out.println("DEBUG away: " + awayInput);


                // 🔥 bruk ORIGINALT navn mot indeks (slik det var før)


// 🔥 fallback: opprett lag hvis de ikke finnes i data


                OddsAnalyzer analyzer = new OddsAnalyzer();

                double valueHome = analyzer.calculateValue(finalHome, oddsHome);
                double valueDraw = analyzer.calculateValue(finalDraw, oddsDraw);
                double valueAway = analyzer.calculateValue(finalAway, oddsAway);

                System.out.println("\n=== VALUE ANALYSE ===");
                System.out.printf("Hjemmeseier value: %.3f\n", valueHome);
                System.out.printf("Uavgjort value:   %.3f\n", valueDraw);
                System.out.printf("Borteseier value: %.3f\n", valueAway);


// 🔹 2️⃣ Finn bet + stake
                String bet = "NO_BET";
                double stake = 0;

                if (valueHome > valueDraw && valueHome > valueAway && valueHome > 0.10) {
                    bet = "HOME";
                    stake = smartStake(finalHome, oddsHome, confidence);
                }
                else if (valueDraw > valueHome && valueDraw > valueAway && valueDraw > 0.10) {
                    bet = "DRAW";
                    stake = smartStake(finalDraw, oddsDraw, confidence);
                }
                else if (valueAway > valueHome && valueAway > valueDraw && valueAway > 0.10) {
                    bet = "AWAY";
                    stake = smartStake(finalAway, oddsAway, confidence);
                }


                System.out.println("Anbefalt spill: " + bet);

                if (stake > 0) {
                    System.out.printf("Stake: %.2f%% av bankroll\n", stake * 100);
                }

                homeInput = TeamNameNormalizer.normalize(homeInput);
                awayInput = TeamNameNormalizer.normalize(awayInput);

// 🔹 3️⃣ Opprett record

// 🔹 4️⃣ Lag PredictionRecord (learning)
                PredictionRecord predictionRecord = new PredictionRecord(
                        homeInput,
                        awayInput,
                        features,
                        finalHome,
                        finalDraw,
                        finalAway,
                        oddsHome,
                        oddsDraw,
                        oddsAway,
                        bet,        // ← STRING først
                        stake,      // ← double etter
                        confidence
                );

                predictionRecord.neuralProbs = neuralProbs;
                predictionRecord.eloProbs = eloProbs;
                predictionRecord.poissonProbs = poissonProbs;
                predictionRecord.oddsProbs = oddsProbs;

// lagre value
                predictionRecord.valueHome = valueHome;
                predictionRecord.valueDraw = valueDraw;
                predictionRecord.valueAway = valueAway;

// 🔹 5️⃣ Lagre i learning-tracker
                tracker.addPrediction(predictionRecord);

// 🔹 6️⃣ Konverter til DB-record
                MatchRecord dbRecord = predictionRecord.toMatchRecord();

// 🔹 7️⃣ Lagre i database
                int id = DatabaseManager.savePrediction(dbRecord);
                predictionRecord.dbId = id;

// 🔹 simulering
                SimulationResult result =
                        analysis.simulateMatch(
                                finalHome,
                                finalDraw,
                                finalAway,
                                1000
                        );

                System.out.println("\n=== SIMULERING 1000 KAMPER ===");
                System.out.println(result);
            }


            else if (valg == 4) {

                System.out.print("Hvilket lag: ");
                String lag = scanner.nextLine();

                TeamStats team = indeks.getOrCreate(lag);
                TeamProfile p = team.getProfile();

                System.out.println("\n--- Oppdater laginfo for " + lag + " ---");

                System.out.print("Antall skadde spillere: ");
                p.setInjuredPlayers(Integer.parseInt(scanner.nextLine()));

                System.out.print("Antall suspenderte: ");
                p.setSuspendedPlayers(Integer.parseInt(scanner.nextLine()));

                System.out.print("Trener rating (0-1): ");
                p.setCoachRating(Double.parseDouble(scanner.nextLine()));

                System.out.print("Motivasjon (0-1): ");
                p.setMotivation(Double.parseDouble(scanner.nextLine()));

                System.out.print("Fatigue/slitenhet (0-1): ");
                p.setFatigue(Double.parseDouble(scanner.nextLine()));

                System.out.println("Laginfo oppdatert!\n");

                TeamDNA dna = team.getDNA();

                System.out.print("Attack style (0-1): ");
                dna.setAttackIndex(Double.parseDouble(scanner.nextLine()));

                System.out.print("Defense solidity (0-1): ");
                dna.setDefenseIndex(Double.parseDouble(scanner.nextLine()));

                System.out.print("Tempo (0-1): ");
                dna.setTempoIndex(Double.parseDouble(scanner.nextLine()));

                System.out.print("Comeback ability (0-1): ");
                dna.setComebackAbility(Double.parseDouble(scanner.nextLine()));

                System.out.print("Choke factor (0-1): ");
                dna.setChokeFactor(Double.parseDouble(scanner.nextLine()));

            }

            else if (valg == 5) {

                System.out.print("Hjemmelag: ");
                String h = scanner.nextLine();

                System.out.print("Bortelag: ");
                String b = scanner.nextLine();

                System.out.print("Odds H: ");
                double oh = Double.parseDouble(scanner.nextLine());

                System.out.print("Odds U: ");
                double od = Double.parseDouble(scanner.nextLine());

                System.out.print("Odds B: ");
                double ob = Double.parseDouble(scanner.nextLine());

                scannerAI.addMatch(new MatchOdds(h,b,oh,od,ob));

                System.out.println("Kamp lagt til scanner.\n");
            }

            else if (valg == 6) {
                scannerAI.scan(indeks);
            }

            else if(valg == 7){

                System.out.print("Hjemmelag: ");
                String h = scanner.nextLine();

                System.out.print("Bortelag: ");
                String b = scanner.nextLine();

                TeamStats home = indeks.getOrCreate(h);
                TeamStats away = indeks.getOrCreate(b);

                System.out.print("Odds H: ");
                double oh = Double.parseDouble(scanner.nextLine());

                System.out.print("Odds U: ");
                double od = Double.parseDouble(scanner.nextLine());

                System.out.print("Odds B: ");
                double ob = Double.parseDouble(scanner.nextLine());

                manualsim.simulateMatch(home, away, oh, od, ob);
            }

            else if(valg == 8){
                manualsim.run(indeks);
            }

            else if(valg == 9){
                autosim.run(indeks);
            }

            else if(valg == 0){
                System.out.println("Lagrer stats...");
                repo.save(indeks);
                System.out.println("Ferdig lagret.");
            }

            else if(valg == 10){
                DatabaseManager.printROIStats();
            }

            else if(valg == 11){
                DatabaseManager.printROIByBetType();
            }

            else if (valg == 12) {

                System.out.println("\nStarter auto pre-match predictions...");

                AutoPredictionEngine.run(indeks, neural, tracker, upcoming);

                System.out.println("Ferdig.\n");
            }

        }
        scanner.close();
    }




    private static int readInt(Scanner scanner){
        while(true){
            try{
                String input = scanner.nextLine();
                return Integer.parseInt(input);
            }catch(Exception e){
                System.out.print("Skriv et tall: ");
            }
        }
    }

    private static double readDouble(Scanner scanner){
        while(true){
            try{
                String input = scanner.nextLine().trim();

                if(input.isEmpty()){
                    System.out.print("Skriv et tall: ");
                    continue;
                }

                return Double.parseDouble(input.replace(",", "."));
            }
            catch(Exception e){
                System.out.print("Ugyldig tall, prøv igjen: ");
            }
        }
    }

    private static void printProbs(String name, double[] p){
        System.out.printf(
                "%s -> H: %.1f%% D: %.1f%% B: %.1f%%\n",
                name,
                p[0]*100,
                p[1]*100,
                p[2]*100
        );
    }

    private static double modelAgreementScore(
            double[] poisson,
            double[] elo,
            double[] neural,
            double[] odds
    ){
        double spread = 0;

        for(int i=0;i<3;i++){

            double mean =
                    (poisson[i] + elo[i] + neural[i] + odds[i]) / 4.0;

            spread += Math.pow(poisson[i] - mean, 2);
            spread += Math.pow(elo[i] - mean, 2);
            spread += Math.pow(neural[i] - mean, 2);
            spread += Math.pow(odds[i] - mean, 2);
        }

        spread = Math.sqrt(spread / 12.0);

        double confidence = 1.0 - (spread * 1.4);

        // 🔥 Winner agreement check
        int winnerCount = 0;

        int pWin = maxIndex(poisson);
        int eWin = maxIndex(elo);
        int nWin = maxIndex(neural);
        int oWin = maxIndex(odds);

        if(pWin == eWin && pWin == nWin && pWin == oWin){
            confidence += 0.05;
        } else {
            confidence -= 0.05;
        }

        return Math.max(0.0, Math.min(1.0, confidence));
    }

    private static int maxIndex(double[] arr){
        int idx = 0;
        for(int i=1;i<arr.length;i++){
            if(arr[i] > arr[idx]){
                idx = i;
            }
        }
        return idx;
    }

    private static double kellyStake(double prob, double odds){

        double edge = (prob * odds - 1) / (odds - 1);

        if(edge <= 0) return 0;

        return edge * 0.25; // quarter Kelly
    }

    private static double smartStake(double prob, double odds, double confidence){

        // Kelly
        double kelly = (prob * odds - 1) / (odds - 1);

        if(kelly <= 0) return 0.0;

        // quarter Kelly
        kelly = kelly * 0.25;

        // confidence justering
        double stake = kelly * confidence;

        // cap maks 3%
        if(stake > 0.03){
            stake = 0.03;
        }

        return stake;   // ⚠️ SKAL være 0.00–0.03
    }

    private static void importHistoricalCsv() {

        importLeague("seriea");
        importLeague("bundesliga");
        importLeague("premierleague");
        importLeague("laliga");
        importLeague("ligue1");
        importLeague("championsleague");
    }

    private static void importLeague(String leagueName) {

        File file = new File("data/historical_" + leagueName + ".csv");

        if (!file.exists()) {
            System.out.println("Fant ikke fil: " + file.getAbsolutePath());
            return;
        }

        List<Match> matches = CsvHistoricalLoader.load(file.getPath(), leagueName);
        System.out.println("Loaded from CSV (" + leagueName + "): " + matches.size());

        try (Connection conn = DatabaseManager.getConnection()) {

            conn.setAutoCommit(false);   // 🔥 viktig

            for (Match m : matches) {
                m.setLeague(leagueName);
                DatabaseManager.saveHistoricalMatch(conn, m); // send conn inn
            }

            conn.commit();               // 🔥 én commit
            conn.setAutoCommit(true);

            System.out.println("Import ferdig: " + leagueName);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }




    private static void importFootballData() {

        Map<String, String> leagues = Map.of(
                "E0", "premierleague",
                "D1", "bundesliga",
                "I1", "seriea",
                "SP1", "laliga",
                "F1", "ligue1"
        );

        for (String code : leagues.keySet()) {

            String leagueName = leagues.get(code);
            File file = new File("data/" + code + ".csv");

            if (!file.exists()) {
                System.out.println("Fant ikke fil: " + file.getAbsolutePath());
                continue;
            }

            List<Match> matches = CsvHistoricalLoader.load(file.getPath(), leagueName);
            System.out.println("Loaded (" + leagueName + "): " + matches.size());

            try (Connection conn = DatabaseManager.getConnection()) {

                conn.setAutoCommit(false);

                for (Match m : matches) {
                    m.setLeague(leagueName);
                    DatabaseManager.saveHistoricalMatch(conn, m);

                    System.out.println(
                            "IMPORT -> " +
                                    m.getHomeTeam() + " vs " + m.getAwayTeam() +
                                    " | odds=" + m.getHomeOdds()
                    );
                }

                conn.commit();

                System.out.println("Import ferdig: " + leagueName);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static StatsIndeks buildStatsFromDB() {

        StatsIndeks stats = new StatsIndeks();

        List<Match> matches = DatabaseManager.getHistoricalMatchesOrdered();

        for (Match m : matches) {
            stats.update(m);
        }

        return stats;


    }

}
