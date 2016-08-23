package augoeides.aqw;

public class Quests {
   private Quests() {
      super();
      throw new UnsupportedOperationException("not allowed to have an instance of this class");
   }

   public static int lookAtValue(String questString, int index) {
      return Integer.parseInt(String.valueOf(questString.charAt(index)), 36);
   }

   public static String updateValue(String questString, int index, int value) {
      String val;
      if(value >= 0 && value < 10) {
         val = String.valueOf(value);
      } else if(value >= 10 && value < 36) {
         val = fromCharCode(new int[]{value + 55});
      } else {
         val = "0";
      }

      return strSetCharAt(questString, index, val);
   }

   private static String strSetCharAt(String _arg1, int _arg2, String _arg3) {
      return _arg1.substring(0, _arg2) + _arg3 + _arg1.substring(_arg2 + 1, _arg1.length());
   }

   public static String fromCharCode(int... codePoints) {
      return new String(codePoints, 0, codePoints.length);
   }
}
