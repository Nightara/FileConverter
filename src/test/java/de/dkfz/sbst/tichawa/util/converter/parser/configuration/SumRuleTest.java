package de.dkfz.sbst.tichawa.util.converter.parser.configuration;

import de.dkfz.sbst.tichawa.util.converter.parser.configuration.Configuration.*;
import org.junit.jupiter.api.*;

import java.time.*;
import java.time.temporal.*;
import java.util.*;

class SumRuleTest
{
  List<SumRule<Integer>> groupOne, groupTwo;
  List<SumRule<Instant>> groupThree, groupFour;

  private static final SumRule<Integer> integerRuleOne = new SumRule<>("inLabel","outLabel",
      DataType.INTEGER,0);
  private static final SumRule<Integer> integerRuleTwo = new SumRule<>("inLabel","outLabel",
      DataType.INTEGER,1);
  private static final SumRule<Integer> integerRuleThree = new SumRule<>("inLabel","outLabel",
      DataType.INTEGER,2);
  private static final SumRule<Integer> integerRuleFour = new SumRule<>("inLabel","outLabel",
      DataType.INTEGER,3);
  private static final SumRule<Integer> integerRuleFive = new SumRule<>("inLabel","outLabel",
      DataType.INTEGER,4);

  private static final SumRule<Instant> instantRuleOne = new SumRule<>("inLabel","outLabel",
      DataType.INSTANT, Instant.EPOCH);
  private static final SumRule<Instant> instantRuleTwo = new SumRule<>("inLabel","outLabel",
      DataType.INSTANT, Instant.EPOCH.plus(2, ChronoUnit.SECONDS));
  private static final SumRule<Instant> instantRuleThree = new SumRule<>("inLabel","outLabel",
      DataType.INSTANT, Instant.EPOCH);
  private static final SumRule<Instant> instantRuleFour = new SumRule<>("inLabel","outLabel",
      DataType.INSTANT, Instant.EPOCH);

  @BeforeEach
  void setupTest()
  {
    groupOne = new LinkedList<>(Arrays.asList(integerRuleOne, integerRuleTwo));
    integerRuleOne.setRuleGroup(groupOne);
    integerRuleTwo.setRuleGroup(groupOne);
    groupTwo = new LinkedList<>(Arrays.asList(integerRuleThree, integerRuleFour));
    integerRuleThree.setRuleGroup(groupTwo);
    integerRuleFour.setRuleGroup(groupTwo);

    groupThree = new LinkedList<>(Arrays.asList(instantRuleOne, instantRuleTwo));
    instantRuleOne.setRuleGroup(groupThree);
    instantRuleTwo.setRuleGroup(groupThree);
    groupFour = new LinkedList<>(Arrays.asList(instantRuleThree, instantRuleFour));
    instantRuleThree.setRuleGroup(groupFour);
    instantRuleFour.setRuleGroup(groupFour);
  }

  @Test
  void addRule()
  {
    int previousSize = integerRuleOne.getRuleGroup().size();
    integerRuleOne.addRule(integerRuleFive);
    Assertions.assertSame(integerRuleOne.getRuleGroup(), integerRuleFive.getRuleGroup());
    Assertions.assertSame(integerRuleOne.getRuleGroup(), integerRuleTwo.getRuleGroup());
    Assertions.assertTrue(integerRuleOne.getRuleGroup().contains(integerRuleFive));
    Assertions.assertEquals(previousSize + 1, integerRuleOne.getRuleGroup().size());
    Assertions.assertEquals(previousSize + 1, groupOne.size());
  }

  @Test
  void canApply()
  {
    Assertions.assertTrue(integerRuleOne.canApply(0));
    Assertions.assertTrue(instantRuleOne.canApply(Instant.now()));
  }

  @Test
  void apply()
  {
    int val1 = 2;
    int val2 = 3;

    Assertions.assertNull(integerRuleOne.apply(val1));
    Assertions.assertEquals(integerRuleTwo.getDefaultVal() + val1 + val2, integerRuleTwo.apply(val2).getData());
    Assertions.assertNull(integerRuleTwo.apply(val2));
    Assertions.assertEquals(integerRuleOne.getDefaultVal() + val1 + val2, integerRuleOne.apply(val1).getData());
    Assertions.assertNull(integerRuleOne.apply(val1));

    Instant val3 = Instant.now();
    Duration val3Duration = Duration.between(Instant.EPOCH, val3);
    Instant val4 = Instant.EPOCH.plus(5, ChronoUnit.MINUTES);
    Duration val4Duration = Duration.between(Instant.EPOCH, val4);

    Assertions.assertNull(instantRuleOne.apply(val3));
    Assertions.assertEquals(instantRuleTwo.getDefaultVal().plus(val3Duration).plus(val4Duration),
        instantRuleTwo.apply(val4).getData());
    Assertions.assertNull(instantRuleTwo.apply(val4));
    Assertions.assertEquals(instantRuleOne.getDefaultVal().plus(val3Duration).plus(val4Duration),
        instantRuleOne.apply(val3).getData());
    Assertions.assertNull(instantRuleOne.apply(val3));
  }

  @Test
  void isGroupFilled()
  {
    int val1 = 2;
    int val2 = 3;

    integerRuleOne.setStash(val1);
    Assertions.assertFalse(integerRuleOne.isGroupFilled());
    Assertions.assertFalse(integerRuleTwo.isGroupFilled());
    integerRuleTwo.setStash(val1);
    Assertions.assertTrue(integerRuleOne.isGroupFilled());
    Assertions.assertTrue(integerRuleTwo.isGroupFilled());
    integerRuleTwo.apply(val2);
    Assertions.assertFalse(integerRuleOne.isGroupFilled());
    Assertions.assertFalse(integerRuleTwo.isGroupFilled());
  }
}