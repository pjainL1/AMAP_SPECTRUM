am = window.am || {};

am.locale = {
    language: 'en',
    dialogs: {
        closeButton: 'Close'
    },
    infoTool: {
        keyDisplayValues: {
            TOTAL_NUM_HHLDS: "Households (Can. Post)",
            SPONSOR_LOCATION_KEY:"AM LOCATION KEY",
            SPONSOR_LOCATION_CODE:"AM LOCATION CODE",
            SPONSOR_LOCATION_NAME:"LOCATION NAME",
            CUSTOMER_LOCATION_CODE :"LOCATION ID"
        }
    },
    apply: {
        nothing: 'Nothing to apply.'
    },
    find: {
        noResult: 'No result found.',
        menu: {
            title: 'Find'
        }
    },
    tradeArea: {
        defineDrive: 'Please define a drive distance of 1 to 250 KM.',
        defineLocation: 'Please define a project location of 1 to 250 KM.',
        unexpected: 'Unexpected trade area type.'
    },
    legends: {
        nwTa: {
            subtitle: {
                nw: 'NW',
                ta: 'TA'
            },
            title: {
                nw: 'Neighbourhood Watch (NW)',
                ta: 'Trade Area (TA)'
            },
            locTemplate: 'Loc. {0}'
        }
    },
    hotspot: {
        filterInvalid: {
            single: "Filter not applied to Collector hotspot.",
            comparison: "Filter not applied to Collector comparison."
        },
        legend: {
            to: 'to',
            km: 'km radius',
            precision: 'Precision',
            per: 'Per'
        }
    },
    storeLevelAnalysis: {
        
        to: 'to',
        from: 'km radius',
        filterInvalid: {
            single: "Filter not applied to Collector hotspot.",
            comparison: "Filter not applied to Collector comparison."
        },
        attributeTitles: {
            collectors : 'Total Collectors',
            spend : 'Total Sales',
            transactions : 'Total Transactions',
            units : 'Total Units',
            collectorsComparison : 'Total Collectors Comparison',
            spendComparison : 'Total Sales Comparison',
            transactionsComparison : 'Total Transactions Comparison',
            unitsComparison : 'Total Units Comparison'
        }, 
        thematicConfiguration: 'Thematic Configuration'
    },
    map: {
        layers: {
            title: 'Base Layer',
            base: 'Grey',
            night: 'Night',
            physical: 'Physical',
            streets: 'Streets',
            hybrid: 'Hybrid',
            satellite: 'Satellite',
            traffic:'Live Traffic',
            transit:'Transit',
            bicycling:'Bicycling'
        }
    },
    selection: {
        please: 'Please make a selection',
        all: 'All selected'
    },
    errors: {
        title: 'Error',
        locations: 'Please select at least 1 location.',
        emptycustom: 'Please draw a custom trade area on the map first.'
    },
    menu: {
        taCustomMsg: "Use the mouse to draw a polygon."
    },
    download: {
        taCsvMsg: "The postal codes in this extract are to be used for AIR MILES purposes.<br/>A postal code is represented by one point on a map. A single postal code can represent very few, or very many households.<br/>For example, in urban areas a postal code can represent a single apartment building or city block. In remote areas it can represent an entire town.",
        msgTitle: "Download"
    },
    tradeAreaInfo: {
        amrp:           'Trade Area: {0}% AMRP spend.',
        armpUnits:      'Trade Area: {0}% AMRP Units.',
        driveDistance:  'Trade Area: {0}km drive distance.',
        projected:      'Trade Area: {0}km projected drive distance.',
        custom:         'Trade Area: Custom/Manual.'
    },
    layerGroup:{
        dynamicLabel:"Dynamic layers"
    },
    console: {
        filterText: 'Start typing to filter the grid',
        tabs: {
            taList: 'Trade Area List',
            locationColors: 'Locations Colors',
            groupLayer: 'Group Layer Management'
        },
        colorManagementConsole: {
            locationID: 'Location ID',
            locationCode: 'Location Code', 
            locationName: 'Location Name',
            city: 'City',
            postalCode: 'Postal Code',
            nwatchColor: 'NW Colour',
            taColor: 'TA Colour',
            sponsorSelect: 'Select a sponsor'
        },
        taHistoryConsole: {
            download: '<b>Download</b>',
            change: '<b>Change</b>',
            id: 'ID',
            date: 'Date',
            rollupName: 'A-Map Rollup Name',
            user: 'Username',
            type: 'Type',
            from: 'From',
            to: 'To',
            location: 'Location',
            locationCode: 'Location Code'
        },
        layerGroup:{
            addBtnLabel: 'Add Group',
            addPromptTitle: 'Add Group',
            addPromptLabel: 'Group name:',
            invalidMsg: 'Empty string is not a valid group name.',
            invalidTitle:'Support Message',
            editTitle:'Edit group',
            editMsg:'Group name:',
            columnTitleOne: 'Group / Layer',
            columnTitleTwo: 'Rename',
            columnTitleThree: 'Delete',
            renameTooltip:'Rename group',
            deleteTooltip:'Delete group',
            moveLayerGroup:'Move group/layer',
            confirm:'Confirmation required',
            confirmDelete:'Are you sure that you want delete the "{0}" group?<br />Any layer currently in this group will be moved to the "Other" group.',
            failure:' failure with status code '
            
        }
    },
    datePickers: {
        defaultButtonText: 'Default',
        period:   'Period 1:',
        periodOne:'Period 1:',
        periodTwo:'Period 2:',
        
        dateRange:'Date Range',
        singleRange:'Single',
        comparisonRange:'Comparison'
    },
    minimumValues:{
        minTransactions:'Min Transactions Per Collector',
        minSpend:'Min Spend Per Collector',
        minUnit:'Min Units Per Collector'
    },
    emailLoadingAlert: {
        subjectLoadingStarted: 'A-Map data processing starting now',
        subjectLoadingTerminatedSuccess: 'A-Map data processing completed successfully',
        subjectLoadingTerminatedError: 'A-Map data processing encountered some errors',
        
        filesMissing: 'Missing files',
        filesEmpty: 'Empty files',
        
        bodyLoadingStarted: '<p>Hello,</p><p>The data processing of the A-Map application just started at %s on %s %s %s.</p>',
        
        bodyLoadingTerminatedSuccessPart1: '<p>Hello,</p>The data processing of the A-Map application just finished in %s at %s on %s %s %s.<p>Here is the information gathered during this data processing:</p>',
        
        noErrorsEncountered: '<p>No errors were encountered during this data processing.</p>',
        errorsEncountered: '<p>The data processing encountered some errors and could not finish.</p>',
       
        bodyTableInformation: 'File',
        bodyTablePreviously: 'Previously',
        bodyTableNewValue: 'New Value',
        bodyTableChange: 'Change',
        
        exceptionText: 'An error occured during the data loading',
        sqlExceptionText: 'A SQL Exception occured while processing the file %s',
        denormalizerError: 'An error occured while running schema denormalizer for schema %s.'
    },
    
    sponsorFilter:{
        noneSelectedText: 'Select sponsor codes',
        selectedText: '# sponsor codes selected'
    }
}