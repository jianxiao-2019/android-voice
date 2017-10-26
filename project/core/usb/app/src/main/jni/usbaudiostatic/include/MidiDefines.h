#ifndef MIDIDEFINES_H
#define MIDIDEFINES_H

#define MIDI_NOTE_ON 0x90 // last 4 bits contain the MIDI channel
#define MIDI_NOTE_OFF 0x80 // last 4 bits contain the MIDI channel
#define MIDI_POLY_PRESSURE 0xA0
#define MIDI_CC 0xB0 // last 4 bits contain the MIDI channel
#define MIDI_PC 0xC0 // last 4 bits contain the MIDI channel
#define MIDI_CHANNEL_PRESSURE 0xD0 // last 4 bits contain the MIDI channel
#define MIDI_PITCH_BEND 0xE0 // last 4 bits contain the MIDI channel
#define MIDI_CC_SUSTAIN_PEDAL 64

// portmidi
typedef int PmTimestamp;

typedef enum {
    pmNoError = 0,
    pmNoData = 0, /**< A "no error" return that also indicates no data avail. */
    pmGotData = 1, /**< A "no error" return that also indicates data available */
    pmHostError = -10000,
    pmInvalidDeviceId, /** out of range or 
                        * output device when input is requested or 
                        * input device when output is requested or
                        * device is already opened 
                        */
    pmInsufficientMemory,
    pmBufferTooSmall,
    pmBufferOverflow,
    pmBadPtr, /* PortMidiStream parameter is NULL or
               * stream is not opened or
               * stream is output when input is required or
               * stream is input when output is required */
    pmBadData, /** illegal midi data, e.g. missing EOX */
    pmInternalError,
    pmBufferMaxSize /** buffer is already as large as it can be */
    /* NOTE: If you add a new error type, be sure to update Pm_GetErrorText() */
} PmError;


typedef int PmDeviceID;
#define pmNoDevice -1
typedef struct {
    int structVersion; /**< this internal structure version */ 
    const char *interf; /**< underlying MIDI API, e.g. MMSystem or DirectX */
    const char *name;   /**< device name, e.g. USB MidiSport 1x1 */
    int input; /**< true iff input is available */
    int output; /**< true iff output is available */
    int opened; /**< used by generic PortMidi code to do error checking on arguments */

} PmDeviceInfo;


/* Filter bit-mask definitions */
/** filter active sensing messages (0xFE): */
#define PM_FILT_ACTIVE (1 << 0x0E)
/** filter system exclusive messages (0xF0): */
#define PM_FILT_SYSEX (1 << 0x00)
/** filter MIDI clock message (0xF8) */
#define PM_FILT_CLOCK (1 << 0x08)
/** filter play messages (start 0xFA, stop 0xFC, continue 0xFB) */
#define PM_FILT_PLAY ((1 << 0x0A) | (1 << 0x0C) | (1 << 0x0B))
/** filter tick messages (0xF9) */
#define PM_FILT_TICK (1 << 0x09)
/** filter undefined FD messages */
#define PM_FILT_FD (1 << 0x0D)
/** filter undefined real-time messages */
#define PM_FILT_UNDEFINED PM_FILT_FD
/** filter reset messages (0xFF) */
#define PM_FILT_RESET (1 << 0x0F)
/** filter all real-time messages */
#define PM_FILT_REALTIME (PM_FILT_ACTIVE | PM_FILT_SYSEX | PM_FILT_CLOCK | \
    PM_FILT_PLAY | PM_FILT_UNDEFINED | PM_FILT_RESET | PM_FILT_TICK)
/** filter note-on and note-off (0x90-0x9F and 0x80-0x8F */
#define PM_FILT_NOTE ((1 << 0x19) | (1 << 0x18))
/** filter channel aftertouch (most midi controllers use this) (0xD0-0xDF)*/
#define PM_FILT_CHANNEL_AFTERTOUCH (1 << 0x1D)
/** per-note aftertouch (0xA0-0xAF) */
#define PM_FILT_POLY_AFTERTOUCH (1 << 0x1A)
/** filter both channel and poly aftertouch */
#define PM_FILT_AFTERTOUCH (PM_FILT_CHANNEL_AFTERTOUCH | PM_FILT_POLY_AFTERTOUCH)
/** Program changes (0xC0-0xCF) */
#define PM_FILT_PROGRAM (1 << 0x1C)
/** Control Changes (CC's) (0xB0-0xBF)*/
#define PM_FILT_CONTROL (1 << 0x1B)
/** Pitch Bender (0xE0-0xEF*/
#define PM_FILT_PITCHBEND (1 << 0x1E)
/** MIDI Time Code (0xF1)*/
#define PM_FILT_MTC (1 << 0x01)
/** Song Position (0xF2) */
#define PM_FILT_SONG_POSITION (1 << 0x02)
/** Song Select (0xF3)*/
#define PM_FILT_SONG_SELECT (1 << 0x03)
/** Tuning request (0xF6)*/
#define PM_FILT_TUNE (1 << 0x06)
/** All System Common messages (mtc, song position, song select, tune request) */
#define PM_FILT_SYSTEMCOMMON (PM_FILT_MTC | PM_FILT_SONG_POSITION | PM_FILT_SONG_SELECT | PM_FILT_TUNE)


#define Pm_Channel(channel) (1<<(channel))

#define Pm_Message(status, data1, data2) \
         ((((data2) << 16) & 0xFF0000) | \
          (((data1) << 8) & 0xFF00) | \
          ((status) & 0xFF))
#define Pm_MessageStatus(msg) ((msg) & 0xFF)
#define Pm_MessageData1(msg) (((msg) >> 8) & 0xFF)
#define Pm_MessageData2(msg) (((msg) >> 16) & 0xFF)

typedef int PmMessage; /**< see PmEvent */

struct PmEvent
{
    PmEvent()
    {
    }

    int      message;
    PmTimestamp    timestamp;
};


struct QueueEntry
{
    PmEvent m_Event;
};


typedef double PaTime;

#endif
