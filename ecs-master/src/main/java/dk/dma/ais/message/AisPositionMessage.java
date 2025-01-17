/* Copyright (c) 2011 Danish Maritime Authority.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dk.dma.ais.message;

import dk.dma.ais.binary.BinArray;
import dk.dma.ais.sentence.Vdm;
import dk.dma.enav.model.geometry.Position;
import dk.dma.ais.binary.SixbitEncoder;
import dk.dma.ais.binary.SixbitException;

/**
 * AIS position message
 * <p>
 * An AIS position message is defined by ITU-R M.1371-4 in annex 8 - AIS messages section 3.1
 * <p>
 * This is a generalization for message types 1-3 and possibly more in the future.
 * <p>
 * Type 1: Scheduled position report (Class A shipborne mobile equipment) Type 2: Assigned scheduled position report
 * (Class A shipborne mobile equipment) Type 3: Special position report, response to interrogation;(Class A shipborne
 * mobile equipment)
 */
public abstract class AisPositionMessage extends AisMessage implements IVesselPositionMessage {

    /** serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /**
     * Navigational status: 0 = under way using engine 1 = at anchor 2 = not under command 3 = restricted
     * manoeuvrability 4 = constrained by her draught 5 = moored 6 = aground 7 = engaged in fishing 8 = under way
     * sailing 9 = reserved for future amendment of navigational status for ships carrying DG, HS, or MP, or IMO hazard
     * or pollutant category C, high speed craft (HSC), 10 = reserved for future amendment of navigational status for
     * ships carrying dangerous goods (DG), harmful substances (HS) or marine pollutants (MP), or IMO hazard or
     * pollutant category A, wing in grand (WIG); 11-13 = reserved for future use 14 = AIS-SART (active) 15 = not
     * defined = default (also used by AIS-SART under test)
     */
    protected int navStatus; // 4 bits

    /**
     * Rate of Turn (ROT): 0 to +126 = turning right at up to 708 degrees per min or higher 0 to -126 = turning left at
     * up to 708 degrees per min or higher Values between 0 and 708 degrees per min coded by ROTais = 4.733
     * SQRT(ROTsensor) degrees per min where ROTsensor is the Rate of Turn as input by an external Rate of Turn
     * Indicator (TI). ROTais is rounded to the nearest integer value.
     * <p>
     * +127 = turning right at more than 5 degrees per 30 s (No TI available) -127 = turning left at more than 5 degrees
     * per 30 s (No TI available) -128 (80 hex) indicates no turn information available (default).
     * <p>
     * ROT data should not be derived from COG information.
     */
    protected int rot; // 8 bits :

    /**
     * Speed Over Ground Speed over ground in 1/10 knot steps (0-102.2 knots) 1023 = not available, 1022 = 102.2 knots
     * or higher
     */
    protected int sog; // 10 bits

    /**
     * AisPosition Accuracy: The position accuracy (PA) flag should be determined in accordance with Table 47 1 = high (
     * =&lt; 10 m) 0 = low (&gt;10 m) 0 = default
     */
    protected int posAcc; // 1 bit

    /**
     * Stores the positions in a general manner
     */
    protected AisPosition pos; // : Lat/Long 1/10000 minute

    /**
     * Course over Ground Course over ground in 1/10 = (0-3599). 3600 (E10h) = not available = default. 3601-4095 should
     * not be used
     */
    protected int cog; // 12 bits

    /**
     * True heading Degrees (0-359) (511 indicates not available = default)
     */
    protected int trueHeading; // 9 bits

    /**
     * Time stamp: UTC second when the report was generated by the EPFS (0-59 or 60 if time stamp is not available,
     * which should also be the default value or 61 if positioning system is in manual input mode or 62 if electronic
     * position fixing system operates in estimated (dead reckoning) mode or 63 if the positioning system is
     * inoperative) 61, 62, 63 are not used by CS AIS
     */
    protected int utcSec; // 6 bits : UTC Seconds

    /**
     * Special manoeuvre indicator: 0 = not available = default 1 = not engaged in special manoeuvre 2 = engaged in
     * special manoeuvre (i.e.: regional passing arrangement on Inland Waterway)
     * <p>
     * NOTE: This field is added in ITU-R M1371-4
     */
    protected int specialManIndicator; // 2 bits

    /**
     * Not used. Should be set to zero. Reserved for future use.
     * <p>
     * NOTE: In ITU-R M1371-4 this field is 3 bits compared to 1 previously
     */
    protected int spare; // 3 bits

    /**
     * RAIM-flag: Receiver autonomous integrity monitoring (RAIM) flag of electronic position fixing device; 0 = RAIM
     * not in use = default; 1 = RAIM in use. See Table 47
     */
    protected int raim; // 1 bit : RAIM flag

    /**
     * SOTDMA/ITDMA sync state: sync state is part of the defined communication state (19) bits. The sync state is the
     * first 2 bits of the 19 bits in the communication state of the message.
     * <p>
     * 0 UTC direct (see 3.1.1.1) 1 UTC indirect (see 3.1.1.2) 2 Station is synchronized to a base station (base direct)
     * 3 Station is synchronized to another station based on the highest number of received stations or to another
     * mobile station, which is directly synchronized to a base station
     */
    protected int syncState; // 2 bits

    /**
     * Instantiates a new Ais position message.
     *
     * @param msgId the msg id
     */
    public AisPositionMessage(int msgId) {
        super(msgId);
    }

    /**
     * Instantiates a new Ais position message.
     *
     * @param vdm the vdm
     */
    public AisPositionMessage(Vdm vdm) {
        super(vdm);
    }

    public void parse(BinArray binArray) throws AisMessageException, SixbitException {
        if (binArray.getLength() < 168) {
            throw new AisMessageException("Message " + msgId + " wrong length: " + binArray.getLength());
        }

        super.parse(binArray);

        /* Parse the pos Message */
        this.navStatus = (int) binArray.getVal(4);
        this.rot = (int) binArray.getVal(8);
        this.sog = (int) binArray.getVal(10);
        this.posAcc = (int) binArray.getVal(1);

        this.pos = new AisPosition();
        this.pos.setRawLongitude(binArray.getVal(28));
        this.pos.setRawLatitude(binArray.getVal(27));

        this.cog = (int) binArray.getVal(12);
        this.trueHeading = (int) binArray.getVal(9);
        this.utcSec = (int) binArray.getVal(6);
        this.specialManIndicator = (int) binArray.getVal(2);
        this.spare = (int) binArray.getVal(3);

        this.raim = (int) binArray.getVal(1);
        this.syncState = (int) binArray.getVal(2);
    }

    /*
     * (non-Javadoc)
     *
     * @see dk.frv.ais.message.AisMessage#encode()
     */
    @Override
    protected SixbitEncoder encode() {
        SixbitEncoder encoder = super.encode();
        encoder.addVal(navStatus, 4);
        encoder.addVal(rot, 8);
        encoder.addVal(sog, 10);
        encoder.addVal(posAcc, 1);
        encoder.addVal(pos.getRawLongitude(), 28);
        encoder.addVal(pos.getRawLatitude(), 27);
        encoder.addVal(cog, 12);
        encoder.addVal(trueHeading, 9);
        encoder.addVal(utcSec, 6);
        encoder.addVal(specialManIndicator, 2);
        encoder.addVal(spare, 3);

        encoder.addVal(raim, 1);
        encoder.addVal(syncState, 2);
        return encoder;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append(super.toString());
        builder.append(", cog=");
        builder.append(cog);
        builder.append(", navStatus=");
        builder.append(navStatus);
        builder.append(", pos=");
        builder.append(pos);
        builder.append(", posAcc=");
        builder.append(posAcc);
        builder.append(", raim=");
        builder.append(raim);
        builder.append(", specialManIndicator=");
        builder.append(specialManIndicator);
        builder.append(", rot=");
        builder.append(rot);
        builder.append(", sog=");
        builder.append(sog);
        builder.append(", spare=");
        builder.append(spare);
        builder.append(", syncState=");
        builder.append(syncState);
        builder.append(", trueHeading=");
        builder.append(trueHeading);
        builder.append(", utcSec=");
        builder.append(utcSec);
        return builder.toString();
    }

    /**
     * Gets nav status.
     *
     * @return the nav status
     */
    public int getNavStatus() {
        return navStatus;
    }

    /**
     * Sets nav status.
     *
     * @param navStatus the nav status
     */
    public void setNavStatus(int navStatus) {
        this.navStatus = navStatus;
    }

    /**
     * Gets rot.
     *
     * @return the rot
     */
    public int getRot() {
        return rot;
    }

    /**
     * Gets sensor rot.
     *
     * @return the sensor rot
     */
    public Float getSensorRot() {
        if (rot == 128) {
            return null;
        }
        int signedRot = (byte)rot;
        float sensorRot = (float)Math.pow(signedRot / 4.733, 2);
        if (signedRot < 0) {
            sensorRot *= -1;
        }
        return sensorRot;
    }

    /**
     * Sets rot.
     *
     * @param rot the rot
     */
    public void setRot(int rot) {
        this.rot = rot;
    }

    public int getSog() {
        return sog;
    }

    /**
     * Sets sog.
     *
     * @param sog the sog
     */
    public void setSog(int sog) {
        this.sog = sog;
    }

    public int getPosAcc() {
        return posAcc;
    }

    /**
     * Sets pos acc.
     *
     * @param posAcc the pos acc
     */
    public void setPosAcc(int posAcc) {
        this.posAcc = posAcc;
    }

    @Override
    public final Position getValidPosition() {
        AisPosition pos = this.pos;
        return pos == null ? null : pos.getGeoLocation();
    }

    public AisPosition getPos() {
        return pos;
    }

    /**
     * Sets pos.
     *
     * @param pos the pos
     */
    public void setPos(AisPosition pos) {
        this.pos = pos;
    }

    public int getCog() {
        return cog;
    }

    /**
     * Sets cog.
     *
     * @param cog the cog
     */
    public void setCog(int cog) {
        this.cog = cog;
    }

    public int getTrueHeading() {
        return trueHeading;
    }

    /**
     * Sets true heading.
     *
     * @param trueHeading the true heading
     */
    public void setTrueHeading(int trueHeading) {
        this.trueHeading = trueHeading;
    }

    public int getUtcSec() {
        return utcSec;
    }

    /**
     * Sets utc sec.
     *
     * @param utcSec the utc sec
     */
    public void setUtcSec(int utcSec) {
        this.utcSec = utcSec;
    }

    /**
     * Gets special man indicator.
     *
     * @return the specialManIndicator
     */
    public int getSpecialManIndicator() {
        return specialManIndicator;
    }

    /**
     * Sets special man indicator.
     *
     * @param specialManIndicator the specialManIndicator to set
     */
    public void setSpecialManIndicator(int specialManIndicator) {
        this.specialManIndicator = specialManIndicator;
    }

    /**
     * Gets spare.
     *
     * @return the spare
     */
    public int getSpare() {
        return spare;
    }

    /**
     * Sets spare.
     *
     * @param spare the spare
     */
    public void setSpare(int spare) {
        this.spare = spare;
    }

    public int getRaim() {
        return raim;
    }

    /**
     * Sets raim.
     *
     * @param raim the raim
     */
    public void setRaim(int raim) {
        this.raim = raim;
    }

    /**
     * Gets sync state.
     *
     * @return the sync state
     */
    public int getSyncState() {
        return syncState;
    }

    /**
     * Sets sync state.
     *
     * @param syncState the sync state
     */
    public void setSyncState(int syncState) {
        this.syncState = syncState;
    }

    public boolean isPositionValid() {
        Position geo = pos.getGeoLocation();
        return geo != null;
    }

    public boolean isCogValid() {
        return cog < 3600;
    }

    public boolean isSogValid() {
        return sog < 1023;
    }

    public boolean isHeadingValid() {
        return trueHeading < 360;
    }

    /**
     * Is rot valid boolean.
     *
     * @return the boolean
     */
    public boolean isRotValid() {
        return rot > -128;
    }
}
