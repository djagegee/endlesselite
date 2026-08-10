package de.shadow.nachtweber;

record ControlProfile(double bossFactor,double eliteFactor,double pvpFactor) {
  enum TargetKind { NORMAL, BOSS, ELITE, PVP_PLAYER }
  ControlProfile { validate(bossFactor); validate(eliteFactor); validate(pvpFactor); }
  static ControlProfile defaults(){return new ControlProfile(0.35,0.65,0.50);}
  double factor(TargetKind kind){return switch(kind){case NORMAL->1.0;case BOSS->bossFactor;case ELITE->eliteFactor;case PVP_PLAYER->pvpFactor;};}
  private static void validate(double value){if(!Double.isFinite(value)||value<0.0||value>1.0)throw new IllegalArgumentException("control factor must be finite in [0,1]");}
}
