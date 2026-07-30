# ZPL-RDR – UML Class Diagram

```mermaid
classDiagram
    direction TB

    %% Interfaces (SPI / Strategy Patterns)
    class ConversionProvider {
        <<interface>>
        +convert(ZplDocument[], Preset) PdfDocument[]*
    }

    class PresetSchema {
        <<interface>>
        +validate(Map~String, String~ fields)*
        +getDefaultValue(String field) Object
        +getFieldNames() Set~String~
    }

    %% Value Objects / Records (Domain Payload)
    class PdfDocument {
        <<record>>
        +byte[] content
    }

    class ZplDocument {
        <<record>>
        +byte[] content
    }

    class ZplLabel {
        <<record>>
        +byte[] content
    }

    class Preset {
        <<record>>
        +String name
        +Map~String, String~ fields
        +getProperty(String field) String
        +withProperty(String field, String value) Preset
    }

    class Dimensions {
        <<record>>
        +int widthMm
        +int heightMm
        +float dpi
        +fromPreset(Preset)$ Dimensions
        +widthDots() int
        +heightDots() int
    }

    class LabelaryClientConfig {
        <<record>>
        +String baseUrl
        +String apiKey
    }

    %% Repository
    class PresetRepository {
        -Path storagePath
        +findById(String) Map~String,String~
        +findAll() Map~String,Map~String,String~~
        +save(String, Map)
        +deleteById(String)
    }

    %% Abstract Base (Template Method)
    class AbstractPresetSchema {
        -Map~String, FieldDefinition~ requiredFields
        +getDefaultValue(String) Object
        +getFieldNames() Set~String~
    }

    class FieldDefinition {
        <<record>>
        +Object defaultValue
    }

    %% Exceptions
    class ZplConverterException
    class ConversionProviderException
    class PresetServiceException
    class PresetSchemaException
    class PresetStorageException
    class PrinterServiceException

    %% Implementations
    class LabelaryConversionProvider {
        -static final int BATCH_SIZE
        -LabelaryClientConfig config
        +convert(ZplDocument[], Preset) PdfDocument[]*
        -splitIntoBatches(ZplDocument) ZplLabel[][]
        -sendBatch(String, ZplLabel[]) PdfDocument
    }

    class LabelaryPresetSchema {
        +validate(Map~String,String~ fields)*
    }

    %% Orchestration
    class ZplConverter {
        -ConversionProvider provider
        +convertAll(InputStream[], Preset) PdfDocument[]*
        -validateFormat(byte[]) void
    }

    class PresetService {
        -Map~String,Preset~ activePresets
        -PresetSchema schema
        -PresetRepository repository
        +getPreset(String) Preset
        +createPreset(String) Preset
        +savePreset(Preset) Preset
        +deletePreset(String) void
    }

    %% Print Service (TSPL-based)
    class PrinterService {
        +print(PdfDocument, String device, Dimensions)
        +printAll(PdfDocument[], String device, Dimensions)
        +listDevices()$ String[]
        -convertPdf(PdfDocument, Dimensions) byte[]
        -sendToPrinter(String, byte[]) void
    }

    class PdfBitmapRenderer {
        +renderPage(PDDocument, int, Dimensions)$ BitmapImage
        +toMonochrome(BufferedImage)$ byte[]
        -scaleImage(BufferedImage, int, int)$ BufferedImage
    }

    class BitmapImage {
        <<record>>
        +int width
        +int height
        +byte[] data
    }

    class TsplLabel {
        +generate(Dimensions, BitmapImage)$ byte[]
    }

    class PrinterDevice {
        +send(String address, byte[])$ void
        +listDevices()$ String[]
        -sendTcp(String, byte[])$ void
    }

    %% GUI Layer
    class GuiApplication {
        -MainPanel mainPanel
        +GuiApplication()
        -createMenuBar()
        -openPresetManager()
        -openAboutDialog()
    }

    class ServiceProvider {
        -static ZplConverter converter
        -static PresetService presetService
        -static PrinterService printerService
        +zplConverter()$ ZplConverter
        +presetService()$ PresetService
        +printerService()$ PrinterService
    }

    class MainPanel {
        -InputPanel inputPanel
        -OutputPanel outputPanel
        -PdfDocument[] lastResult
        -Dimensions currentDims
        +onConvert()
        +onConvertAndPrint()
        +onPrintResults()
        +onPdfsLoaded(List~Path~)
        +clearLastResult()
        +refreshPresets()
        +requestAddFiles()
        -resolvePreset() Preset
        -resolvePresetSilently() Preset
        -closeStreams(InputStream[]) void
    }

    class InputPanel {
        -ZplFileTableModel fileTableModel
        -JComboBox~String~ presetCombo
        +addFiles()
        +loadPdfs()
        +removeSelected()
        +refreshPresets()
        +getSelectedPaths() List~Path~
        +getSelectedPresetName() String
    }

    class OutputPanel {
        -PdfResultTableModel resultTableModel
        -JComboBox~String~ printerCombo
        -JProgressBar progressBar
        +showResults(PdfDocument[])
        +clearResults()
        +setProgress(int)
        +setPrinting(boolean)
        +refreshPrinters()
        +getSelectedDevice() String
        -saveResults()
    }

    class PresetManagerDialog {
        -PresetService presetService
        -boolean modified
        +PresetManagerDialog(Frame, PresetService)
        +isModified() boolean
        -loadPresetList()
        -onCreate()
        -onEdit()
        -onDelete()
        -editPresetFields(Preset, boolean) Preset
    }

    class ConvertWorker {
        -InputStream[] files
        -Preset preset
        -BiConsumer~PdfDocument[],Throwable~ callback
        +ConvertWorker(InputStream[], Preset, BiConsumer)
        +doInBackground() PdfDocument[]
    }

    class PrintWorker {
        -PdfDocument[] documents
        -String device
        -Dimensions dims
        -BiConsumer~Void,Throwable~ callback
        +PrintWorker(PdfDocument[], String, Dimensions, BiConsumer)
        +doInBackground() Void
    }

    class PrinterListWorker {
        -BiConsumer~String[],Throwable~ callback
        +PrinterListWorker(BiConsumer)
        +doInBackground() String[]
    }

    class FormatUtil {
        +formatSize(long)$ String
    }

    %% Relationships — Implementations
    ConversionProvider <|.. LabelaryConversionProvider : implements
    PresetSchema <|.. AbstractPresetSchema : implements
    AbstractPresetSchema <|-- LabelaryPresetSchema : extends
    AbstractPresetSchema *-- FieldDefinition : inner record

    %% Relationships — Core
    ZplConverter --> ConversionProvider : delegates
    ZplConverter --> Preset : uses
    ZplConverter ..> ZplDocument : creates
    ZplConverter ..> ZplConverterException : throws

    PresetService --> PresetSchema : uses
    PresetService --> PresetRepository : uses
    PresetService --> Preset : manages
    PresetService ..> PresetServiceException : throws

    PresetRepository ..> PresetStorageException : throws

    LabelaryConversionProvider --> LabelaryClientConfig : configured by
    LabelaryConversionProvider ..> ZplLabel : partitions into
    LabelaryConversionProvider ..> PdfDocument : produces
    LabelaryConversionProvider ..> ConversionProviderException : throws

    %% Relationships — Print
    PrinterService --> PdfBitmapRenderer : uses
    PrinterService --> TsplLabel : uses
    PrinterService --> PrinterDevice : uses
    PrinterService --> Dimensions : uses
    PrinterService ..> PrinterServiceException : throws
    PdfBitmapRenderer ..> BitmapImage : returns

    %% Relationships — GUI
    GuiApplication --> MainPanel : contains
    GuiApplication --> PresetManagerDialog : opens
    GuiApplication ..> ServiceProvider : uses

    ServiceProvider ..> ZplConverter : builds
    ServiceProvider ..> PresetService : builds
    ServiceProvider ..> PrinterService : builds

    MainPanel --> InputPanel : contains
    MainPanel --> OutputPanel : contains
    MainPanel --> ConvertWorker : starts
    MainPanel --> PrintWorker : starts
    MainPanel ..> ServiceProvider : uses
    MainPanel --> Dimensions : creates

    InputPanel ..> ServiceProvider : uses

    OutputPanel --> PrinterListWorker : starts

    PresetManagerDialog --> PresetService : delegates

    ConvertWorker ..> ZplConverter : calls
    PrintWorker ..> PrinterService : calls
    PrinterListWorker ..> PrinterService : calls

    PdfResultTableModel ..> FormatUtil : uses
    ZplFileTableModel ..> FormatUtil : uses

    %% Inheritance — Exceptions
    ZplConverterException --|> Exception
    ConversionProviderException --|> Exception
    PresetServiceException --|> Exception
    PresetSchemaException --|> Exception
    PresetStorageException --|> Exception
    PrinterServiceException --|> Exception
```
