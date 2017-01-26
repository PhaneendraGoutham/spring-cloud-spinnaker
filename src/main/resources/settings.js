'use strict';

var feedbackUrl = 'http://localhost';
var gateHost = '{gate}';
var bakeryDetailUrl = process.env.BAKERY_DETAIL_URL || 'http://bakery.test.netflix.net/#/?region={{context.region}}&package={{context.package}}&detail=bake:{{context.status.resourceId}}';
var authEndpoint = '{gate}/auth/user';


window.spinnakerSettings = {
  checkForUpdates: true,
  defaultProviders: ['cf'],
  feedbackUrl: feedbackUrl,
  gateUrl: gateHost,
  bakeryDetailUrl: bakeryDetailUrl,
  authEndpoint: authEndpoint,
  pollSchedule: 30000,
  defaultTimeZone: 'America/Los_Angeles', // see http://momentjs.com/timezone/docs/#/data-utilities/
  defaultCategory: 'serverGroup',
  providers: {
    cf: {
      defaults: {
        account: '{primaryAccount}',
        region: '{defaultOrg}',
        zone: '{primaryAccount}'
      },
      primaryAccounts: '{primaryAccounts}'
    },
  },
  whatsNew: {
    gistId: '32526cd608db3d811b38',
    fileName: 'news.md',
  },
  notifications: {
    email: {
      enabled: true,
    },
    hipchat: {
      enabled: true,
      botName: 'Skynet T-800'
    },
    sms: {
      enabled: true,
    },
    slack: {
      enabled: true,
      botName: 'spinnakerbot'
    }
  },
  authEnabled: '{oauth.enabled}' === 'true', // convert string to boolean
  gitSources: ['stash', 'github'],
  triggerTypes: ['git', 'pipeline', 'docker', 'cron', 'jenkins', 'travis'],
  feature: {
    pipelines: true,
    notifications: false,
    fastProperty: true,
    vpcMigrator: true,
    clusterDiff: true,
    roscoMode: false,
    netflixMode: false,
    // whether stages affecting infrastructure (like "Create Load Balancer") should be enabled or not
    infrastructureStages: process.env.INFRA_STAGES === 'enabled',
    jobs: false,
  },
};
